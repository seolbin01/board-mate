import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { getGameDetail, getConversation } from '../api/rulemaster';
import type { BggGameDetail, BggGameSearchResult } from '../types';
import GameSearchInput from '../components/rulemaster/GameSearchInput';
import GameInfoCard from '../components/rulemaster/GameInfoCard';
import RuleMasterChat from '../components/rulemaster/RuleMasterChat';

export default function RuleMasterPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [selectedGame, setSelectedGame] = useState<BggGameDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [loadingConversation, setLoadingConversation] = useState(false);

  // URL 파라미터로 게임 ID가 있으면 자동 로드
  useEffect(() => {
    const bggId = searchParams.get('bggId');
    if (bggId) {
      loadGame(Number(bggId));
    }
  }, [searchParams]);

  const loadGame = async (bggId: number) => {
    setLoading(true);
    setLoadingConversation(true);
    try {
      // 게임 상세 정보 로드
      const game = await getGameDetail(bggId);
      setSelectedGame(game);

      // 대화 히스토리 로드 (있으면)
      const conversation = await getConversation(bggId);
      if (conversation?.messages) {
        // 훅에서 메시지 설정 (useRuleMasterChat의 setMessages 사용)
        // 현재는 새로고침 시 대화 내역이 표시됩니다
      }
    } catch (error) {
      console.error('게임 정보 로드 실패:', error);
      alert('게임 정보를 불러오는데 실패했습니다');
    } finally {
      setLoading(false);
      setLoadingConversation(false);
    }
  };

  const handleSelectGame = async (game: BggGameSearchResult) => {
    loadGame(game.bggId);
  };

  const handleChangeGame = () => {
    setSelectedGame(null);
    navigate('/rulemaster', { replace: true });
  };

  const handleBack = () => {
    navigate(-1);
  };

  return (
    <div className="max-w-7xl mx-auto">
      {/* 헤더 */}
      <div className="mb-6 flex items-center gap-4">
        <button
          onClick={handleBack}
          className="p-2 hover:bg-stone-100 rounded-lg transition-colors"
        >
          <ArrowLeft size={24} className="text-stone-600" />
        </button>
        <div>
          <h1 className="text-3xl font-bold text-stone-800">룰마스터</h1>
          <p className="text-stone-600 mt-1">
            보드게임 규칙과 전략을 AI와 함께 알아보세요
          </p>
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 size={48} className="text-orange-500 animate-spin" />
        </div>
      ) : !selectedGame ? (
        /* 게임 선택 화면 */
        <div className="max-w-2xl mx-auto py-12">
          <div className="bg-white rounded-2xl shadow-lg border border-stone-200 p-8">
            <div className="text-center mb-8">
              <div className="w-20 h-20 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-4xl">🎲</span>
              </div>
              <h2 className="text-2xl font-bold text-stone-800 mb-2">
                어떤 게임이 궁금하신가요?
              </h2>
              <p className="text-stone-600">
                게임 이름을 검색하고 규칙을 물어보세요
              </p>
            </div>
            <GameSearchInput onSelectGame={handleSelectGame} />
          </div>
        </div>
      ) : (
        /* 게임 선택 후: 2컬럼 레이아웃 */
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 왼쪽: 게임 정보 */}
          <div className="lg:col-span-1">
            <GameInfoCard game={selectedGame} onChangeGame={handleChangeGame} />
          </div>

          {/* 오른쪽: 채팅 */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-md border border-stone-200 p-6 h-[600px] lg:h-[700px]">
              {loadingConversation ? (
                <div className="flex items-center justify-center h-full">
                  <Loader2 size={48} className="text-orange-500 animate-spin" />
                </div>
              ) : (
                <RuleMasterChat
                  bggId={selectedGame.bggId}
                  gameName={selectedGame.nameKorean || selectedGame.name}
                  onClear={() => {
                    // 대화 초기화 후 처리 (필요시)
                  }}
                />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
