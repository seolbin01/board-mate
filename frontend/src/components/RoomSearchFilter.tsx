import { useState, useEffect } from 'react';
import { Search, RotateCcw, MapPin, Gamepad2, Calendar } from 'lucide-react';
import client from '../api/client';
import type { ApiResponse, Game, RoomSearchParams } from '../types';

interface Props {
  onSearch: (params: RoomSearchParams) => void;
}

export default function RoomSearchFilter({ onSearch }: Props) {
  const [games, setGames] = useState<Game[]>([]);
  const [region, setRegion] = useState('');
  const [gameId, setGameId] = useState('');
  const [date, setDate] = useState('');

  useEffect(() => {
    const fetchGames = async () => {
      try {
        const response = await client.get<ApiResponse<Game[]>>('/games');
        setGames(response.data.data);
      } catch (error) {
        console.error('게임 목록 조회 실패:', error);
      }
    };
    fetchGames();
  }, []);

  const handleSearch = () => {
    const params: RoomSearchParams = {};
    if (region.trim()) params.region = region.trim();
    if (gameId) params.gameId = Number(gameId);
    if (date) params.date = date;
    onSearch(params);
  };

  const handleReset = () => {
    setRegion('');
    setGameId('');
    setDate('');
    onSearch({});
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-amber-100 p-5 mb-6">
      <div className="grid gap-4 md:grid-cols-4">
        <div>
          <label className="flex items-center gap-1.5 text-sm font-medium text-gray-700 mb-2">
            <MapPin className="w-3.5 h-3.5 text-orange-500" />
            <span>지역</span>
          </label>
          <input
            type="text"
            value={region}
            onChange={(e) => setRegion(e.target.value)}
            placeholder="예: 강남, 홍대"
            className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-shadow"
          />
        </div>

        <div>
          <label className="flex items-center gap-1.5 text-sm font-medium text-gray-700 mb-2">
            <Gamepad2 className="w-3.5 h-3.5 text-orange-500" />
            <span>게임</span>
          </label>
          <select
            value={gameId}
            onChange={(e) => setGameId(e.target.value)}
            className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-shadow"
          >
            <option value="">전체 게임</option>
            {games.map((game) => (
              <option key={game.id} value={game.id}>
                {game.title}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="flex items-center gap-1.5 text-sm font-medium text-gray-700 mb-2">
            <Calendar className="w-3.5 h-3.5 text-orange-500" />
            <span>날짜</span>
          </label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-shadow"
          />
        </div>

        <div className="flex items-end gap-2">
          <button
            onClick={handleSearch}
            className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-orange-500 text-white font-medium rounded-xl hover:bg-orange-600 transition-colors shadow-sm hover:shadow"
          >
            <Search className="w-4 h-4" />
            <span>검색</span>
          </button>
          <button
            onClick={handleReset}
            className="flex items-center justify-center gap-1.5 px-4 py-2 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
