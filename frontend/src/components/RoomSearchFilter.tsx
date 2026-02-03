import { useState, useEffect } from 'react';
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
    <div className="bg-white rounded-lg shadow p-4 mb-6">
      <div className="grid gap-4 md:grid-cols-4">
        <div>
          <label className="block text-sm font-medium mb-1">지역</label>
          <input
            type="text"
            value={region}
            onChange={(e) => setRegion(e.target.value)}
            placeholder="예: 강남, 홍대"
            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">게임</label>
          <select
            value={gameId}
            onChange={(e) => setGameId(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
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
          <label className="block text-sm font-medium mb-1">날짜</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div className="flex items-end gap-2">
          <button
            onClick={handleSearch}
            className="flex-1 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
          >
            검색
          </button>
          <button
            onClick={handleReset}
            className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
          >
            초기화
          </button>
        </div>
      </div>
    </div>
  );
}
