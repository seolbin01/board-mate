import { useState, useRef, useEffect } from 'react';
import { Send, Trash2, Loader2, Sparkles } from 'lucide-react';
import { useRuleMasterChat } from '../../hooks/useSSE';
import { clearConversation } from '../../api/rulemaster';
import MessageBubble from './MessageBubble';
import StreamingMessage from './StreamingMessage';

interface RuleMasterChatProps {
  bggId: number;
  gameName: string;
  onClear?: () => void;
}

export default function RuleMasterChat({ bggId, gameName, onClear }: RuleMasterChatProps) {
  const [input, setInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);
  const {
    messages,
    isStreaming,
    streamingContent,
    sendMessage,
    clearMessages,
  } = useRuleMasterChat(bggId);

  // 채팅 스크롤
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, streamingContent]);

  const handleSend = async () => {
    if (!input.trim() || isStreaming) return;

    const message = input.trim();
    setInput('');
    await sendMessage(message);
  };

  const handleClear = async () => {
    if (!confirm('대화 내역을 모두 삭제하시겠습니까?')) return;

    try {
      await clearConversation(bggId);
      clearMessages();
      onClear?.();
    } catch (error) {
      console.error('대화 초기화 실패:', error);
      alert('대화 초기화에 실패했습니다');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* 헤더 */}
      <div className="flex justify-between items-center mb-4 pb-4 border-b border-stone-200">
        <div>
          <h2 className="text-lg font-bold text-stone-800 flex items-center gap-2">
            <Sparkles size={20} className="text-orange-500" />
            룰마스터
          </h2>
          <p className="text-sm text-stone-500">{gameName}에 대해 물어보세요</p>
        </div>
        <button
          onClick={handleClear}
          className="px-3 py-2 text-sm bg-stone-100 text-stone-600 rounded-lg hover:bg-stone-200 transition-colors flex items-center gap-2"
          disabled={messages.length === 0 && !isStreaming}
        >
          <Trash2 size={16} />
          초기화
        </button>
      </div>

      {/* 메시지 리스트 */}
      <div className="flex-1 overflow-y-auto mb-4 px-2">
        {messages.length === 0 && !isStreaming ? (
          <div className="flex flex-col items-center justify-center h-full text-center py-12">
            <div className="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mb-4">
              <Sparkles size={32} className="text-orange-500" />
            </div>
            <h3 className="text-lg font-semibold text-stone-800 mb-2">
              룰마스터에게 물어보세요
            </h3>
            <p className="text-sm text-stone-500 max-w-md">
              게임의 규칙, 전략, 플레이 팁 등 궁금한 점을 질문해주세요.
              <br />
              AI가 자세히 답변해드립니다.
            </p>
            <div className="mt-6 space-y-2">
              <button
                onClick={() => setInput('이 게임의 기본 규칙을 설명해주세요')}
                className="block w-full px-4 py-2 text-sm bg-orange-50 text-orange-700 rounded-lg hover:bg-orange-100 transition-colors"
              >
                "이 게임의 기본 규칙을 설명해주세요"
              </button>
              <button
                onClick={() => setInput('초보자를 위한 전략을 알려주세요')}
                className="block w-full px-4 py-2 text-sm bg-orange-50 text-orange-700 rounded-lg hover:bg-orange-100 transition-colors"
              >
                "초보자를 위한 전략을 알려주세요"
              </button>
            </div>
          </div>
        ) : (
          <>
            {messages.map((msg, idx) => (
              <MessageBubble key={idx} message={msg} />
            ))}
            {isStreaming && <StreamingMessage content={streamingContent} />}
          </>
        )}
        <div ref={chatEndRef} />
      </div>

      {/* 입력창 */}
      <div className="flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={isStreaming ? 'AI가 답변 중입니다...' : '메시지를 입력하세요...'}
          className="flex-1 px-4 py-3 border border-stone-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 disabled:bg-stone-50 disabled:text-stone-400"
          disabled={isStreaming}
          maxLength={500}
        />
        <button
          onClick={handleSend}
          disabled={!input.trim() || isStreaming}
          className="px-4 py-3 bg-orange-600 text-white rounded-xl hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
        >
          {isStreaming ? (
            <Loader2 size={20} className="animate-spin" />
          ) : (
            <Send size={20} />
          )}
        </button>
      </div>
    </div>
  );
}
