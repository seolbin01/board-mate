import client from './client';
import type { ApiResponse, BggGameSearchResult, BggGameDetail, ConversationSession } from '../types';

// 게임 검색
export const searchGames = async (query: string, limit = 10): Promise<BggGameSearchResult[]> => {
  const response = await client.get<ApiResponse<BggGameSearchResult[]>>(
    '/rulemaster/games/search',
    { params: { query, limit } }
  );
  return response.data.data;
};

// 게임 상세 조회
export const getGameDetail = async (bggId: number): Promise<BggGameDetail> => {
  const response = await client.get<ApiResponse<BggGameDetail>>(`/rulemaster/games/${bggId}`);
  return response.data.data;
};

// 대화 히스토리 조회
export const getConversation = async (bggId: number): Promise<ConversationSession | null> => {
  try {
    const response = await client.get<ApiResponse<ConversationSession>>(
      '/rulemaster/conversations',
      { params: { bggId } }
    );
    return response.data.data;
  } catch {
    return null;
  }
};

// 대화 초기화
export const clearConversation = async (bggId: number): Promise<void> => {
  await client.delete('/rulemaster/conversations', { params: { bggId } });
};

// SSE 채팅 (EventSource 사용)
export const createChatStream = (
  bggId: number,
  message: string,
  onMessage: (content: string) => void,
  onDone: (messageId: string) => void,
  onError: (error: { code: string; message: string }) => void
): AbortController => {
  const controller = new AbortController();
  const token = localStorage.getItem('accessToken');

  fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/rulemaster/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream',
    },
    body: JSON.stringify({ bggId, message }),
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
            if (data.type === 'content' && data.content) {
              onMessage(data.content);
            } else if (data.type === 'done') {
              onDone(data.messageId || '');
            } else if (data.type === 'error') {
              onError(data.error);
            }
          } catch (e) {
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
