import { Users, Clock, BarChart3, RefreshCw } from 'lucide-react';
import type { BggGameDetail } from '../../types';

interface GameInfoCardProps {
  game: BggGameDetail;
  onChangeGame: () => void;
}

export default function GameInfoCard({ game, onChangeGame }: GameInfoCardProps) {
  // 난이도 표시 (weight: 1-5)
  const getDifficultyLabel = (weight: number) => {
    if (weight < 2) return { label: '쉬움', color: 'text-emerald-600' };
    if (weight < 3) return { label: '보통', color: 'text-amber-600' };
    if (weight < 4) return { label: '어려움', color: 'text-orange-600' };
    return { label: '매우 어려움', color: 'text-rose-600' };
  };

  const difficulty = getDifficultyLabel(game.weight);

  return (
    <div className="bg-white rounded-xl shadow-md border border-stone-200 overflow-hidden">
      {/* 게임 이미지 */}
      {game.imageUrl && (
        <div className="relative h-48 bg-stone-100">
          <img
            src={game.imageUrl}
            alt={game.name}
            className="w-full h-full object-contain"
          />
        </div>
      )}

      <div className="p-6">
        {/* 게임명 */}
        <div className="mb-4">
          <h2 className="text-2xl font-bold text-stone-800 mb-1">
            {game.nameKorean || game.name}
          </h2>
          {game.nameKorean && (
            <p className="text-sm text-stone-500">{game.name}</p>
          )}
          <p className="text-xs text-stone-400 mt-1">
            출시: {game.yearPublished} · BGG 평점: {game.averageRating.toFixed(1)}
          </p>
        </div>

        {/* 게임 정보 */}
        <div className="grid grid-cols-3 gap-4 mb-4">
          <div className="flex flex-col items-center p-3 bg-orange-50 rounded-lg">
            <Users size={20} className="text-orange-600 mb-1" />
            <p className="text-xs text-stone-600 mb-1">플레이어</p>
            <p className="text-sm font-semibold text-stone-800">
              {game.minPlayers === game.maxPlayers
                ? `${game.minPlayers}명`
                : `${game.minPlayers}-${game.maxPlayers}명`}
            </p>
          </div>
          <div className="flex flex-col items-center p-3 bg-orange-50 rounded-lg">
            <Clock size={20} className="text-orange-600 mb-1" />
            <p className="text-xs text-stone-600 mb-1">플레이 시간</p>
            <p className="text-sm font-semibold text-stone-800">
              {game.playingTime}분
            </p>
          </div>
          <div className="flex flex-col items-center p-3 bg-orange-50 rounded-lg">
            <BarChart3 size={20} className="text-orange-600 mb-1" />
            <p className="text-xs text-stone-600 mb-1">난이도</p>
            <p className={`text-sm font-semibold ${difficulty.color}`}>
              {difficulty.label}
            </p>
          </div>
        </div>

        {/* 메카닉 태그 */}
        {game.mechanics.length > 0 && (
          <div className="mb-4">
            <p className="text-xs font-semibold text-stone-600 mb-2">메카닉</p>
            <div className="flex flex-wrap gap-2">
              {game.mechanics.slice(0, 5).map((mechanic, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 bg-stone-100 text-stone-700 rounded-full text-xs"
                >
                  {mechanic}
                </span>
              ))}
              {game.mechanics.length > 5 && (
                <span className="px-3 py-1 bg-stone-100 text-stone-500 rounded-full text-xs">
                  +{game.mechanics.length - 5}
                </span>
              )}
            </div>
          </div>
        )}

        {/* 카테고리 태그 */}
        {game.categories.length > 0 && (
          <div className="mb-4">
            <p className="text-xs font-semibold text-stone-600 mb-2">카테고리</p>
            <div className="flex flex-wrap gap-2">
              {game.categories.slice(0, 5).map((category, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-xs"
                >
                  {category}
                </span>
              ))}
              {game.categories.length > 5 && (
                <span className="px-3 py-1 bg-orange-100 text-orange-600 rounded-full text-xs">
                  +{game.categories.length - 5}
                </span>
              )}
            </div>
          </div>
        )}

        {/* 새 게임 선택 버튼 */}
        <button
          onClick={onChangeGame}
          className="w-full py-3 mt-2 bg-stone-100 text-stone-700 rounded-xl hover:bg-stone-200 font-medium transition-colors flex items-center justify-center gap-2"
        >
          <RefreshCw size={18} />
          새 게임 선택
        </button>
      </div>
    </div>
  );
}
