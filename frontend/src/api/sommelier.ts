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

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const text = decoder.decode(value);
      const lines = text.split('\n');

      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const data = JSON.parse(line.slice(5));
            if (data.type === 'text' && data.content) {
              onMessage(data.content);
            } else if (data.type === 'done' || data.completed) {
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
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError({ code: 'NETWORK_ERROR', message: err.message });
    }
  });

  return controller;
};
