import { useState, useRef, useMemo, useLayoutEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Send, Trash2, Loader2, Wine } from 'lucide-react';
import { useSommelierChat } from '../hooks/useSommelierChat';
import { clearHistory } from '../api/sommelier';
import MessageBubble from '../components/sommelier/MessageBubble';
import StreamingMessage from '../components/sommelier/StreamingMessage';

export default function SommelierPage() {
  const navigate = useNavigate();
  const [input, setInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);

  // 세션 ID 생성 (브라우저 세션 유지)
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

  const isInitialLoad = useRef(true);

  // 채팅 스크롤 - 메시지 변경 시 맨 아래로
  useLayoutEffect(() => {
    if (messages.length > 0 || streamingContent) {
      // 초기 로드 시에는 즉시, 이후에는 부드럽게
      const behavior = isInitialLoad.current ? 'instant' : 'smooth';

      // requestAnimationFrame으로 DOM 렌더링 완료 후 스크롤
      requestAnimationFrame(() => {
        chatEndRef.current?.scrollIntoView({ behavior });
      });

      if (isInitialLoad.current && messages.length > 0) {
        isInitialLoad.current = false;
      }
    }
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
      await clearHistory(sessionId);
      clearMessages();
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

  const handleBack = () => {
    navigate(-1);
  };

  const suggestedQuestions = [
    '2인용 30분 이내 전략 게임 추천해줘',
    '카탄이랑 비슷한 게임 뭐 있어?',
    '초보자도 쉽게 배울 수 있는 파티 게임 알려줘',
    '스플렌더랑 아줄 비교해줘',
  ];

  return (
    <div className="max-w-4xl mx-auto h-[calc(100vh-120px)] flex flex-col">
      {/* 헤더 */}
      <div className="mb-4 flex items-center gap-4">
        <button
          onClick={handleBack}
          className="p-2 hover:bg-stone-100 rounded-lg transition-colors"
        >
          <ArrowLeft size={24} className="text-stone-600" />
        </button>
        <div className="flex-1">
          <h1 className="text-2xl font-bold text-stone-800 flex items-center gap-2">
            <Wine size={28} className="text-purple-600" />
            보드게임 소믈리에
          </h1>
          <p className="text-stone-600 text-sm">
            취향에 맞는 보드게임을 추천받아보세요
          </p>
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

      {/* 채팅 영역 */}
      <div className="flex-1 bg-white rounded-2xl shadow-lg border border-stone-200 p-6 flex flex-col min-h-0">
        {/* 메시지 리스트 */}
        <div className="flex-1 overflow-y-auto mb-4 px-2">
          {messages.length === 0 && !isStreaming ? (
            <div className="flex flex-col items-center justify-center h-full text-center py-8">
              <div className="w-20 h-20 bg-purple-100 rounded-full flex items-center justify-center mb-4">
                <Wine size={40} className="text-purple-600" />
              </div>
              <h3 className="text-xl font-semibold text-stone-800 mb-2">
                어떤 게임을 찾고 계신가요?
              </h3>
              <p className="text-stone-500 max-w-md mb-6">
                인원, 시간, 난이도, 테마 등 원하는 조건을 말씀해주시면
                <br />
                딱 맞는 게임을 추천해드릴게요!
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 w-full max-w-lg">
                {suggestedQuestions.map((question, idx) => (
                  <button
                    key={idx}
                    onClick={() => setInput(question)}
                    className="px-4 py-3 text-sm bg-purple-50 text-purple-700 rounded-xl hover:bg-purple-100 transition-colors text-left"
                  >
                    "{question}"
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
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={isStreaming ? '소믈리에가 답변 중입니다...' : '어떤 게임을 찾으시나요?'}
            className="flex-1 px-4 py-3 border border-stone-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500 disabled:bg-stone-50 disabled:text-stone-400"
            disabled={isStreaming}
            maxLength={500}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || isStreaming}
            className="px-4 py-3 bg-purple-600 text-white rounded-xl hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          >
            {isStreaming ? (
              <Loader2 size={20} className="animate-spin" />
            ) : (
              <Send size={20} />
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
