import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Building2, Calendar, Users, FileText, Gamepad2, Plus } from 'lucide-react';
import client from '../api/client';
import type { ApiResponse } from '../types';

interface Game {
  id: number;
  title: string;
  minPlayers: number;
  maxPlayers: number;
}

export default function CreateRoomPage() {
  const [games, setGames] = useState<Game[]>([]);
  const [gameId, setGameId] = useState('');
  const [region, setRegion] = useState('');
  const [cafeName, setCafeName] = useState('');
  const [gameDate, setGameDate] = useState('');
  const [maxParticipants, setMaxParticipants] = useState(4);
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const navigate = useNavigate();

  useEffect(() => {
    const fetchGames = async () => {
      const response = await client.get<ApiResponse<Game[]>>('/games');
      setGames(response.data.data);
    };
    fetchGames();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await client.post('/rooms', {
        gameId: Number(gameId),
        region,
        cafeName,
        gameDate,
        maxParticipants,
        description,
      });
      navigate(`/rooms/${response.data.data.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || '방 생성에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">방 만들기</h1>
        <p className="text-gray-600">새로운 보드게임 모임을 시작해보세요</p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-100 text-red-700 rounded-xl">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            게임 선택
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Gamepad2 className="h-5 w-5 text-orange-400" />
            </div>
            <select
              value={gameId}
              onChange={(e) => setGameId(e.target.value)}
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all appearance-none bg-white"
              required
            >
              <option value="">게임을 선택하세요</option>
              {games.map((game) => (
                <option key={game.id} value={game.id}>
                  {game.title} ({game.minPlayers}-{game.maxPlayers}명)
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            지역
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <MapPin className="h-5 w-5 text-orange-400" />
            </div>
            <input
              type="text"
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              placeholder="예: 서울 강남"
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            카페 이름
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Building2 className="h-5 w-5 text-orange-400" />
            </div>
            <input
              type="text"
              value={cafeName}
              onChange={(e) => setCafeName(e.target.value)}
              placeholder="예: 레드버튼 강남점"
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            날짜/시간
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Calendar className="h-5 w-5 text-orange-400" />
            </div>
            <input
              type="datetime-local"
              value={gameDate}
              onChange={(e) => setGameDate(e.target.value)}
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            최대 인원
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Users className="h-5 w-5 text-orange-400" />
            </div>
            <input
              type="number"
              value={maxParticipants}
              onChange={(e) => setMaxParticipants(Number(e.target.value))}
              min={2}
              max={10}
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            설명
          </label>
          <div className="relative">
            <div className="absolute top-3 left-0 pl-4 flex items-start pointer-events-none">
              <FileText className="h-5 w-5 text-orange-400" />
            </div>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="모임에 대한 설명을 입력하세요"
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all resize-none"
              rows={3}
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-3.5 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-semibold rounded-xl hover:from-orange-600 hover:to-orange-700 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-orange-200 transition-all flex items-center justify-center gap-2"
        >
          <Plus className="w-5 h-5" />
          {loading ? '생성 중...' : '방 만들기'}
        </button>
      </form>
    </div>
  );
}