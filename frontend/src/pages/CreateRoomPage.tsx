import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
      <h1 className="text-2xl font-bold mb-6">방 만들기</h1>

      {error && (
        <div className="mb-4 p-3 bg-red-100 text-red-700 rounded">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">게임 선택</label>
          <select
            value={gameId}
            onChange={(e) => setGameId(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg"
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

        <div>
          <label className="block text-sm font-medium mb-1">지역</label>
          <input
            type="text"
            value={region}
            onChange={(e) => setRegion(e.target.value)}
            placeholder="예: 서울 강남"
            className="w-full px-3 py-2 border rounded-lg"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">카페 이름</label>
          <input
            type="text"
            value={cafeName}
            onChange={(e) => setCafeName(e.target.value)}
            placeholder="예: 레드버튼 강남점"
            className="w-full px-3 py-2 border rounded-lg"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">날짜/시간</label>
          <input
            type="datetime-local"
            value={gameDate}
            onChange={(e) => setGameDate(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">최대 인원</label>
          <input
            type="number"
            value={maxParticipants}
            onChange={(e) => setMaxParticipants(Number(e.target.value))}
            min={2}
            max={10}
            className="w-full px-3 py-2 border rounded-lg"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">설명</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="모임에 대한 설명을 입력하세요"
            className="w-full px-3 py-2 border rounded-lg"
            rows={3}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50"
        >
          {loading ? '생성 중...' : '방 만들기'}
        </button>
      </form>
    </div>
  );
}