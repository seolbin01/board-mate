import { useState, useEffect, useRef } from 'react';
import { X, Loader2, Search, Send, RotateCcw, ChevronLeft } from 'lucide-react';
import { searchGames, getGameDetail, createChatStream, clearConversation, getConversation } from '../api/rulemaster';
import type { BggGameSearchResult, BggGameDetail, RuleMasterMessage } from '../types';

interface RuleMasterOverlayProps {
  isOpen: boolean;
  onClose: () => void;
  initialBggId?: number;
}

export default function RuleMasterOverlay({ isOpen, onClose, initialBggId }: RuleMasterOverlayProps) {
  const [selectedGame, setSelectedGame] = useState<BggGameDetail | null>(null);
  const [loading, setLoading] = useState(false);

  // 검색 관련
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<BggGameSearchResult[]>([]);
  const [searching, setSearching] = useState(false);

  // 채팅 관련
  const [messages, setMessages] = useState<RuleMasterMessage[]>([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [streamingContent, setStreamingContent] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 초기 게임 로드
  useEffect(() => {
    if (isOpen && initialBggId && !selectedGame) {
      loadGame(initialBggId);
    }
  }, [isOpen, initialBggId]);

  // 오버레이 닫힐 때 상태 초기화
  useEffect(() => {
    if (!isOpen) {
      setSearchQuery('');
      setSearchResults([]);
    }
  }, [isOpen]);

  // 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, streamingContent]);

  const loadGame = async (bggId: number) => {
    setLoading(true);
    try {
      const game = await getGameDetail(bggId);
      setSelectedGame(game);

      // 대화 히스토리 로드
      const conversation = await getConversation(bggId);
      if (conversation?.messages) {
        setMessages(conversation.messages);
      }
    } catch (error) {
      console.error('게임 정보 로드 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim() || searching) return;

    setSearching(true);
    try {
      const results = await searchGames(searchQuery.trim());
      setSearchResults(results);
    } catch (error) {
      console.error('검색 실패:', error);
    } finally {
      setSearching(false);
    }
  };

  const handleSelectGame = (game: BggGameSearchResult) => {
    setSearchResults([]);
    setSearchQuery('');
    loadGame(game.bggId);
  };

  const handleSend = () => {
    if (!input.trim() || !selectedGame || sending) return;

    const userMessage: RuleMasterMessage = {
      role: 'user',
      content: input.trim(),
      timestamp: new Date().toISOString(),
    };

    setMessages(prev => [...prev, userMessage]);
    const messageText = input.trim();
    setInput('');
    setSending(true);
    setStreamingContent('');

    createChatStream(
      selectedGame.bggId,
      messageText,
      (content: string) => {
        setStreamingContent(prev => prev + content);
      },
      () => {
        setSending(false);
      },
      (error: { code: string; message: string }) => {
        console.error('채팅 오류:', error);
        setSending(false);
      }
    );
  };

  // 스트리밍 완료 시 메시지 추가
  useEffect(() => {
    if (!sending && streamingContent) {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: streamingContent,
        timestamp: new Date().toISOString(),
      }]);
      setStreamingContent('');
    }
  }, [sending]);

  const handleClear = async () => {
    if (!selectedGame || !confirm('대화 내용을 초기화할까요?')) return;

    try {
      await clearConversation(selectedGame.bggId);
      setMessages([]);
    } catch (error) {
      console.error('초기화 실패:', error);
    }
  };

  const handleBack = () => {
    setSelectedGame(null);
    setMessages([]);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-end p-4 sm:p-6 pointer-events-none">
      {/* 오버레이 배경 */}
      <div
        className="absolute inset-0 bg-black/20 pointer-events-auto"
        onClick={onClose}
      />

      {/* 채팅창 */}
      <div className="relative w-full max-w-md h-[600px] bg-white rounded-2xl shadow-2xl flex flex-col pointer-events-auto overflow-hidden">
        {/* 헤더 */}
        <div className="flex items-center justify-between px-4 py-3 bg-orange-500 text-white">
          <div className="flex items-center gap-2">
            {selectedGame && (
              <button onClick={handleBack} className="p-1 hover:bg-orange-600 rounded-lg transition-colors">
                <ChevronLeft size={20} />
              </button>
            )}
            <span className="text-lg font-bold">🎲 룰마스터</span>
          </div>
          <div className="flex items-center gap-1">
            {selectedGame && (
              <button
                onClick={handleClear}
                className="p-2 hover:bg-orange-600 rounded-lg transition-colors"
                title="대화 초기화"
              >
                <RotateCcw size={18} />
              </button>
            )}
            <button
              onClick={onClose}
              className="p-2 hover:bg-orange-600 rounded-lg transition-colors"
            >
              <X size={20} />
            </button>
          </div>
        </div>

        {loading ? (
          <div className="flex-1 flex items-center justify-center">
            <Loader2 size={40} className="text-orange-500 animate-spin" />
          </div>
        ) : !selectedGame ? (
          /* 게임 선택 화면 */
          <div className="flex-1 flex flex-col p-4">
            <div className="text-center mb-6">
              <p className="text-stone-600">어떤 게임의 규칙이 궁금하세요?</p>
            </div>

            {/* 검색창 */}
            <div className="flex gap-2 mb-4">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                placeholder="게임 이름 검색..."
                className="flex-1 px-4 py-2 border border-stone-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              />
              <button
                onClick={handleSearch}
                disabled={searching || !searchQuery.trim()}
                className="px-4 py-2 bg-orange-500 text-white rounded-xl hover:bg-orange-600 disabled:opacity-50 transition-colors"
              >
                {searching ? <Loader2 size={20} className="animate-spin" /> : <Search size={20} />}
              </button>
            </div>

            {/* 검색 결과 */}
            <div className="flex-1 overflow-y-auto space-y-2">
              {searchResults.map((game) => (
                <button
                  key={game.bggId}
                  onClick={() => handleSelectGame(game)}
                  className="w-full flex items-center gap-3 p-3 bg-stone-50 hover:bg-orange-50 rounded-xl transition-colors text-left"
                >
                  {game.thumbnailUrl && (
                    <img src={game.thumbnailUrl} alt="" className="w-12 h-12 rounded-lg object-cover" />
                  )}
                  <div>
                    <p className="font-medium text-stone-800">{game.name}</p>
                    {game.yearPublished && (
                      <p className="text-sm text-stone-500">{game.yearPublished}년</p>
                    )}
                  </div>
                </button>
              ))}
              {searchResults.length === 0 && searchQuery && !searching && (
                <p className="text-center text-stone-400 py-8">검색 결과가 없습니다</p>
              )}
            </div>
          </div>
        ) : (
          /* 채팅 화면 */
          <>
            {/* 게임 정보 */}
            <div className="px-4 py-2 bg-orange-50 border-b border-orange-100">
              <p className="font-medium text-orange-800 truncate">
                {selectedGame.nameKorean || selectedGame.name}
              </p>
            </div>

            {/* 메시지 목록 */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.length === 0 && !streamingContent && (
                <div className="text-center text-stone-400 py-8">
                  <p>👋 안녕하세요!</p>
                  <p className="text-sm mt-1">{selectedGame.nameKorean || selectedGame.name}에 대해 물어보세요</p>
                </div>
              )}

              {messages.map((msg, idx) => (
                <div
                  key={idx}
                  className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[85%] px-4 py-2 rounded-2xl ${
                      msg.role === 'user'
                        ? 'bg-orange-500 text-white rounded-br-md'
                        : 'bg-stone-100 text-stone-800 rounded-bl-md'
                    }`}
                  >
                    <p className="text-sm whitespace-pre-wrap">{msg.content}</p>
                  </div>
                </div>
              ))}

              {/* 스트리밍 중인 메시지 */}
              {streamingContent && (
                <div className="flex justify-start">
                  <div className="max-w-[85%] px-4 py-2 rounded-2xl bg-stone-100 text-stone-800 rounded-bl-md">
                    <p className="text-sm whitespace-pre-wrap">{streamingContent}</p>
                  </div>
                </div>
              )}

              {/* 로딩 인디케이터 */}
              {sending && !streamingContent && (
                <div className="flex justify-start">
                  <div className="px-4 py-2 rounded-2xl bg-stone-100 rounded-bl-md">
                    <Loader2 size={20} className="text-orange-500 animate-spin" />
                  </div>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>

            {/* 입력창 */}
            <div className="p-3 border-t border-stone-200">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
                      e.preventDefault();
                      handleSend();
                    }
                  }}
                  placeholder="규칙이나 전략을 물어보세요..."
                  className="flex-1 px-4 py-2 border border-stone-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm"
                  disabled={sending}
                />
                <button
                  onClick={handleSend}
                  disabled={!input.trim() || sending}
                  className="px-4 py-2 bg-orange-500 text-white rounded-xl hover:bg-orange-600 disabled:opacity-50 transition-colors"
                >
                  <Send size={18} />
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
