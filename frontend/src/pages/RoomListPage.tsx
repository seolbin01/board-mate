import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import RoomSearchFilter from '../components/RoomSearchFilter';
import type { Room, ApiResponse, RoomSearchParams } from '../types';

export default function RoomListPage() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchRooms = useCallback(async (params: RoomSearchParams = {}) => {
    setLoading(true);
    try {
      const queryParams = new URLSearchParams();
      if (params.region) queryParams.append('region', params.region);
      if (params.gameId) queryParams.append('gameId', String(params.gameId));
      if (params.date) queryParams.append('date', params.date);

      const queryString = queryParams.toString();
      const url = queryString ? `/rooms?${queryString}` : '/rooms';

      const response = await client.get<ApiResponse<Room[]>>(url);
      setRooms(response.data.data);
    } catch (error) {
      console.error('방 목록 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRooms();
  }, [fetchRooms]);

  const handleSearch = (params: RoomSearchParams) => {
    fetchRooms(params);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">모임 찾기</h1>
        <Link
          to="/rooms/new"
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
        >
          + 방 만들기
        </Link>
      </div>

      <RoomSearchFilter onSearch={handleSearch} />

      {loading ? (
        <div className="text-center py-10">로딩 중...</div>
      ) : rooms.length === 0 ? (
        <div className="text-center py-10 text-gray-500">
          검색 결과가 없습니다
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {rooms.map((room) => (
            <Link
              key={room.id}
              to={`/rooms/${room.id}`}
              className="block p-4 bg-white rounded-lg shadow hover:shadow-md transition"
            >
              <div className="flex justify-between items-start mb-2">
                <span className="px-2 py-1 text-xs bg-indigo-100 text-indigo-700 rounded">
                  {room.gameTitle}
                </span>
                <span className={`text-sm ${
                  room.roomStatus === 'FULL' ? 'text-red-500' : 'text-green-500'
                }`}>
                  {room.currentParticipants}/{room.maxParticipants}명
                </span>
              </div>

              <h3 className="font-medium mb-1">{room.region}</h3>
              <p className="text-sm text-gray-500 mb-2">{room.cafeName}</p>

              <div className="text-sm text-gray-600">
                {new Date(room.gameDate).toLocaleDateString('ko-KR', {
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </div>

              <div className="text-sm text-gray-500 mt-2">
                방장: {room.hostNickname}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
