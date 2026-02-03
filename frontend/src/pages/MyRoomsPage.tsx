import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Calendar, User, Users, Loader2 } from 'lucide-react';
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
        return <span className="px-3 py-1 text-xs font-medium bg-green-100 text-green-700 rounded-lg">대기중</span>;
      case 'FULL':
        return <span className="px-3 py-1 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-lg">모집완료</span>;
      case 'CLOSED':
        return <span className="px-3 py-1 text-xs font-medium bg-gray-100 text-gray-700 rounded-lg">종료</span>;
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Loader2 className="animate-spin text-orange-500" size={40} />
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-stone-800 mb-6">내 모임</h1>

      {rooms.length === 0 ? (
        <div className="text-center py-10 text-stone-500">
          참가한 모임이 없습니다
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {rooms.map((room) => (
            <Link
              key={room.id}
              to={`/rooms/${room.id}`}
              className="group block p-5 bg-white rounded-xl border border-stone-200 shadow-md hover:shadow-lg hover:border-orange-300 transition-all duration-300 hover:-translate-y-0.5"
            >
              <div className="flex justify-between items-start mb-3">
                <span className="px-3 py-1 text-xs font-medium bg-orange-100 text-orange-700 rounded-lg">
                  {room.gameTitle}
                </span>
                {getStatusBadge(room.roomStatus)}
              </div>

              <div className="flex items-start gap-2 mb-2">
                <MapPin size={18} className="text-orange-500 mt-0.5 flex-shrink-0" />
                <div>
                  <h3 className="font-semibold text-stone-800 group-hover:text-orange-600 transition-colors">{room.region}</h3>
                  <p className="text-sm text-stone-500">{room.cafeName}</p>
                </div>
              </div>

              <div className="flex items-center gap-2 text-sm text-stone-600 mb-2">
                <Calendar size={16} className="text-orange-500 flex-shrink-0" />
                <span>
                  {new Date(room.gameDate).toLocaleDateString('ko-KR', {
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </span>
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-stone-100">
                <div className="flex items-center gap-2 text-sm text-stone-500">
                  <User size={16} className="text-orange-500 flex-shrink-0" />
                  <span>{room.hostNickname}</span>
                </div>
                <div className="flex items-center gap-1 text-sm font-medium text-stone-600">
                  <Users size={16} className="text-orange-500" />
                  <span>{room.currentParticipants}/{room.maxParticipants}</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
