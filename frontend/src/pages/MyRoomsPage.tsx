import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client';
import type { Room, ApiResponse } from '../types';

export default function MyRoomsPage() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMyRooms = async () => {
      try {
        const response = await client.get<ApiResponse<Room[]>>('/rooms/my');
        setRooms(response.data.data);
      } catch (error) {
        console.error('내 모임 조회 실패:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchMyRooms();
  }, []);

  const getStatusBadge = (status: Room['roomStatus']) => {
    switch (status) {
      case 'WAITING':
        return <span className="px-2 py-1 text-xs bg-green-100 text-green-700 rounded">대기중</span>;
      case 'FULL':
        return <span className="px-2 py-1 text-xs bg-yellow-100 text-yellow-700 rounded">모집완료</span>;
      case 'CLOSED':
        return <span className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded">종료</span>;
      default:
        return null;
    }
  };

  if (loading) {
    return <div className="text-center py-10">로딩 중...</div>;
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">내 모임</h1>

      {rooms.length === 0 ? (
        <div className="text-center py-10 text-gray-500">
          참가한 모임이 없습니다
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
                {getStatusBadge(room.roomStatus)}
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

              <div className="flex justify-between items-center mt-2">
                <span className="text-sm text-gray-500">
                  방장: {room.hostNickname}
                </span>
                <span className="text-sm text-gray-500">
                  {room.currentParticipants}/{room.maxParticipants}명
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
