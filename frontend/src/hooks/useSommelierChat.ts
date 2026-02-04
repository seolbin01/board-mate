import { useState, useCallback, useRef, useEffect } from 'react';
import { flushSync } from 'react-dom';
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
        console.log('[DEBUG] onDone called, content length:', streamingContentRef.current.length);
        const assistantContent = streamingContentRef.current;
        streamingContentRef.current = '';

        // flushSync로 모든 상태 업데이트를 동기적으로 처리
        // 이렇게 하면 messages 업데이트 전에 streamingContent가 사라지는 문제 방지
        flushSync(() => {
          if (assistantContent) {
            setMessages(prev => {
              console.log('[DEBUG] setMessages called, prev:', prev.length);
              return [
                ...prev,
                {
                  role: 'assistant',
                  content: assistantContent,
                  timestamp: new Date().toISOString(),
                }
              ];
            });
          }
          setStreamingContent('');
          setIsStreaming(false);
        });
      },
      (error) => {
        console.error('Sommelier chat error:', error);
        const content = streamingContentRef.current || '죄송합니다, 오류가 발생했습니다. 다시 시도해주세요.';
        streamingContentRef.current = '';

        flushSync(() => {
          setMessages(prev => [
            ...prev,
            {
              role: 'assistant',
              content,
              timestamp: new Date().toISOString(),
            }
          ]);
          setStreamingContent('');
          setIsStreaming(false);
        });
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
