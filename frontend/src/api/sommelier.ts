import client from './client';
import type { ApiResponse } from '../types';

export interface SommelierMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

// 대화 히스토리 조회
export const getHistory = async (sessionId: string): Promise<SommelierMessage[]> => {
  try {
    const response = await client.get<ApiResponse<SommelierMessage[]>>(
      `/sommelier/history/${sessionId}`
    );
    return response.data.data;
  } catch {
    return [];
  }
};

// 대화 초기화
export const clearHistory = async (sessionId: string): Promise<void> => {
  await client.delete(`/sommelier/history/${sessionId}`);
};

// SSE 채팅
export const createSommelierStream = (
  sessionId: string,
  message: string,
  onMessage: (content: string) => void,
  onDone: () => void,
  onError: (error: { code: string; message: string }) => void
): AbortController => {
  const controller = new AbortController();
  const token = localStorage.getItem('accessToken');

  fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/sommelier/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream',
    },
    body: JSON.stringify({ sessionId, message }),
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.ok) {
      onError({ code: 'HTTP_ERROR', message: `HTTP ${response.status}` });
      return;
    }

    const reader = response.body?.getReader();
    const decoder = new TextDecoder();

    if (!reader) return;

    let doneHandled = false;
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');

      // 마지막 라인은 불완전할 수 있으므로 버퍼에 보관
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const jsonStr = line.slice(5).trim();
            if (!jsonStr) continue;
            const data = JSON.parse(jsonStr);
            if (data.type === 'text' && data.content) {
              onMessage(data.content);
            } else if (data.type === 'done' || data.completed) {
              doneHandled = true;
              onDone();
            } else if (data.type === 'error') {
              onError({ code: 'API_ERROR', message: data.content || 'Unknown error' });
            }
          } catch {
            // 파싱 에러 무시
          }
        }
      }
    }

    // 남은 버퍼 처리
    if (buffer.startsWith('data:')) {
      try {
        const jsonStr = buffer.slice(5).trim();
        if (jsonStr) {
          const data = JSON.parse(jsonStr);
          if (data.type === 'done' || data.completed) {
            doneHandled = true;
            onDone();
          }
        }
      } catch {
        // 무시
      }
    }

    // 스트림이 끝났는데 onDone이 호출되지 않았으면 호출
    if (!doneHandled) {
      onDone();
    }
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError({ code: 'NETWORK_ERROR', message: err.message });
    }
  });

  return controller;
};
