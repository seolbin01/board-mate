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

    if (!reader) {
      onDone();
      return;
    }

    let buffer = '';
    let isDone = false;

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        // SSE 이벤트는 \n\n으로 구분됨
        const events = buffer.split('\n\n');
        // 마지막은 불완전할 수 있으므로 버퍼에 보관
        buffer = events.pop() || '';

        for (const event of events) {
          const lines = event.split('\n');
          for (const line of lines) {
            if (line.startsWith('data:')) {
              try {
                const jsonStr = line.slice(5).trim();
                if (!jsonStr) continue;
                const data = JSON.parse(jsonStr);
                if (data.type === 'text' && data.content) {
                  onMessage(data.content);
                } else if (data.type === 'done') {
                  // done 메시지 받으면 즉시 완료 처리
                  isDone = true;
                  onDone();
                  return; // 스트림 읽기 종료
                } else if (data.type === 'error') {
                  onError({ code: 'API_ERROR', message: data.content || 'Unknown error' });
                  return;
                }
              } catch {
                // 파싱 에러 무시
              }
            }
          }
        }
      }
    } finally {
      // done 메시지로 이미 처리되지 않은 경우에만 onDone 호출
      if (!isDone) {
        onDone();
      }
    }
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError({ code: 'NETWORK_ERROR', message: err.message });
    }
  });

  return controller;
};
