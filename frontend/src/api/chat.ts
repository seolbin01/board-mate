import client from './client';
import type { ApiResponse, ChatMessage } from '../types';

export const chatApi = {
  getChatHistory: async (roomId: number) => {
    const response = await client.get<ApiResponse<ChatMessage[]>>(`/rooms/${roomId}/chats`);
    return response.data.data;
  },

  sendMessage: async (roomId: number, content: string) => {
    const response = await client.post<ApiResponse<ChatMessage>>(`/rooms/${roomId}/chats`, {
      content,
    });
    return response.data.data;
  },
};
