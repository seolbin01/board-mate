import { useState, useCallback, useRef, useEffect } from 'react';
import { createSommelierStream, getHistory, type SommelierMessage } from '../api/sommelier';

export const useSommelierChat = (sessionId: string) => {
  const [messages, setMessages] = useState<SommelierMessage[]>([]);
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamingContent, setStreamingContent] = useState('');
  const controllerRef = useRef<AbortController | null>(null);
  const streamingContentRef = useRef('');

  // 초기 히스토리 로드
  useEffect(() => {
    const loadHistory = async () => {
      const history = await getHistory(sessionId);
      if (history.length > 0) {
        setMessages(history);
      }
    };
    loadHistory();
  }, [sessionId]);

  const sendMessage = useCallback(async (content: string) => {
    // 사용자 메시지 추가
    const userMessage: SommelierMessage = {
      role: 'user',
      content,
      timestamp: new Date().toISOString(),
    };
    setMessages(prev => [...prev, userMessage]);
    setIsStreaming(true);
    setStreamingContent('');
    streamingContentRef.current = '';

    controllerRef.current = createSommelierStream(
      sessionId,
      content,
      (chunk) => {
        streamingContentRef.current += chunk;
        setStreamingContent(streamingContentRef.current);
      },
      () => {
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
        console.error('Sommelier chat error:', error);
        setMessages(prev => [
          ...prev,
          {
            role: 'assistant',
            content: '죄송합니다, 오류가 발생했습니다. 다시 시도해주세요.',
            timestamp: new Date().toISOString(),
          }
        ]);
        setIsStreaming(false);
      }
    );
  }, [sessionId]);

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
