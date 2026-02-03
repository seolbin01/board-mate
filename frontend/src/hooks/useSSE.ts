import { useState, useCallback, useRef } from 'react';
import { createChatStream } from '../api/rulemaster';
import type { RuleMasterMessage } from '../types';

export const useRuleMasterChat = (bggId: number) => {
  const [messages, setMessages] = useState<RuleMasterMessage[]>([]);
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamingContent, setStreamingContent] = useState('');
  const controllerRef = useRef<AbortController | null>(null);
  const streamingContentRef = useRef('');

  const sendMessage = useCallback(async (content: string) => {
    // 사용자 메시지 추가
    const userMessage: RuleMasterMessage = {
      role: 'user',
      content,
      timestamp: new Date().toISOString(),
    };
    setMessages(prev => [...prev, userMessage]);
    setIsStreaming(true);
    setStreamingContent('');
    streamingContentRef.current = '';

    controllerRef.current = createChatStream(
      bggId,
      content,
      (chunk) => {
        streamingContentRef.current += chunk;
        setStreamingContent(streamingContentRef.current);
      },
      (_messageId) => {
        setMessages(prev => [
          ...prev,
          {
            role: 'assistant',
            content: streamingContentRef.current,
            timestamp: new Date().toISOString(),
          }
        ]);
        setStreamingContent('');
        streamingContentRef.current = '';
        setIsStreaming(false);
      },
      (error) => {
        console.error('Chat error:', error);
        setIsStreaming(false);
      }
    );
  }, [bggId]);

  const stopStreaming = useCallback(() => {
    controllerRef.current?.abort();
    setIsStreaming(false);
  }, []);

  const clearMessages = useCallback(() => {
    setMessages([]);
  }, []);

  return {
    messages,
    isStreaming,
    streamingContent,
    sendMessage,
    stopStreaming,
    clearMessages,
    setMessages,
  };
};
