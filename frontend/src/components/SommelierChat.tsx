import { useState, useRef, useEffect, useMemo } from 'react';
import { Send, Trash2, Loader2, Wine, Minimize2 } from 'lucide-react';
import { useSommelierChat } from '../hooks/useSommelierChat';
import { clearHistory } from '../api/sommelier';
import MessageBubble from './sommelier/MessageBubble';
import StreamingMessage from './sommelier/StreamingMessage';

interface SommelierChatProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function SommelierChat({ isOpen, onClose }: SommelierChatProps) {
  const [input, setInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);

  const sessionId = useMemo(() => {
    const stored = sessionStorage.getItem('sommelier-session-id');
    if (stored) return stored;
    const newId = crypto.randomUUID();
    sessionStorage.setItem('sommelier-session-id', newId);
    return newId;
  }, []);

  const {
    messages,
    isStreaming,
    streamingContent,
    sendMessage,
    clearMessages,
  } = useSommelierChat(sessionId);

  // 채팅창 열릴 때 또는 메시지 변경 시 스크롤
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => {
        chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      }, 50);
    }
  }, [isOpen, messages, streamingContent]);

  const handleSend = async () => {
    if (!input.trim() || isStreaming) return;
    const message = input.trim();
    setInput('');
    await sendMessage(message);
  };

  const handleClear = async () => {
    if (!confirm('대화 내역을 모두 삭제하시겠습니까?')) return;
    try {
      await clearHistory(sessionId);
      clearMessages();
    } catch (error) {
      console.error('대화 초기화 실패:', error);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault();
      handleSend();
    }
  };

  const suggestedQuestions = [
    '2인용 전략 게임 추천해줘',
    '초보자용 파티 게임 알려줘',
  ];

  if (!isOpen) return null;

  return (
    <div className="fixed bottom-24 right-6 w-96 h-[500px] bg-white rounded-2xl shadow-2xl border border-stone-200 flex flex-col z-50 overflow-hidden">
      {/* 헤더 */}
      <div className="bg-purple-600 text-white px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Wine size={20} />
          <span className="font-semibold">보드게임 소믈리에</span>
        </div>
        <div className="flex items-center gap-1">
          <button
            onClick={handleClear}
            className="p-1.5 hover:bg-purple-500 rounded-lg transition-colors"
            title="대화 초기화"
          >
            <Trash2 size={16} />
          </button>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-purple-500 rounded-lg transition-colors"
            title="닫기"
          >
            <Minimize2 size={16} />
          </button>
        </div>
      </div>

      {/* 채팅 영역 */}
      <div className="flex-1 overflow-y-auto p-4 bg-stone-50">
        {messages.length === 0 && !isStreaming ? (
          <div className="flex flex-col items-center justify-center h-full text-center">
            <div className="w-14 h-14 bg-purple-100 rounded-full flex items-center justify-center mb-3">
              <Wine size={28} className="text-purple-600" />
            </div>
            <p className="text-stone-600 text-sm mb-4">
              어떤 게임을 찾고 계신가요?
            </p>
            <div className="flex flex-col gap-2 w-full">
              {suggestedQuestions.map((q, idx) => (
                <button
                  key={idx}
                  onClick={() => setInput(q)}
                  className="px-3 py-2 text-xs bg-purple-50 text-purple-700 rounded-lg hover:bg-purple-100 transition-colors text-left"
                >
                  "{q}"
                </button>
              ))}
            </div>
          </div>
        ) : (
          <>
            {messages.map((msg, idx) => (
              <MessageBubble
                key={idx}
                message={{
                  role: msg.role,
                  content: msg.content,
                  timestamp: msg.timestamp,
                }}
              />
            ))}
            {isStreaming && <StreamingMessage content={streamingContent} />}
          </>
        )}
        <div ref={chatEndRef} />
      </div>

      {/* 입력창 */}
      <div className="p-3 border-t border-stone-200 bg-white">
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={isStreaming ? '답변 중...' : '메시지 입력...'}
            className="flex-1 px-3 py-2 text-sm border border-stone-200 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 disabled:bg-stone-50"
            disabled={isStreaming}
            maxLength={500}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || isStreaming}
            className="px-3 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {isStreaming ? (
              <Loader2 size={18} className="animate-spin" />
            ) : (
              <Send size={18} />
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
