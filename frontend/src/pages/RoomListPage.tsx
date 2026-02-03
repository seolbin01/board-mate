import { useEffect, useState, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import { Plus, MapPin, Calendar, User, Users, Loader2 } from 'lucide-react';
import client from '../api/client';
import RoomSearchFilter from '../components/RoomSearchFilter';
import type { Room, ApiResponse, RoomSearchParams, PageResponse } from '../types';

export default function RoomListPage() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentFilters, setCurrentFilters] = useState<RoomSearchParams>({});
  const observerRef = useRef<HTMLDivElement>(null);

  const fetchRooms = useCallback(async (params: RoomSearchParams = {}, pageNum: number = 0, isInitial: boolean = false) => {
    if (isInitial) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }

    try {
      const queryParams = new URLSearchParams();
      if (params.region) queryParams.append('region', params.region);
      if (params.gameId) queryParams.append('gameId', String(params.gameId));
      if (params.date) queryParams.append('date', params.date);
      queryParams.append('page', String(pageNum));
      queryParams.append('size', '10');

      const queryString = queryParams.toString();
      const url = `/rooms?${queryString}`;

      const response = await client.get<ApiResponse<PageResponse<Room>>>(url);
      const pageData = response.data.data;

      if (isInitial) {
        setRooms(pageData.content);
      } else {
        setRooms(prev => [...prev, ...pageData.content]);
      }

      setHasMore(!pageData.last);
      setPage(pageData.page);
    } catch (error) {
      console.error('방 목록 조회 실패:', error);
    } finally {
      if (isInitial) {
        setLoading(false);
      } else {
        setLoadingMore(false);
      }
    }
  }, []);

  const loadMore = useCallback(() => {
    if (!loadingMore && hasMore) {
      fetchRooms(currentFilters, page + 1, false);
    }
  }, [loadingMore, hasMore, page, currentFilters, fetchRooms]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loadingMore && !loading) {
          loadMore();
        }
      },
      { threshold: 0.1 }
    );

    if (observerRef.current) {
      observer.observe(observerRef.current);
    }

    return () => observer.disconnect();
  }, [hasMore, loadingMore, loading, loadMore]);

  useEffect(() => {
    fetchRooms(currentFilters, 0, true);
  }, []);

  const handleSearch = (params: RoomSearchParams) => {
    setCurrentFilters(params);
    setPage(0);
    setHasMore(true);
    fetchRooms(params, 0, true);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-stone-800">모임 찾기</h1>
        <Link
          to="/rooms/new"
          className="flex items-center gap-2 px-4 py-2 bg-orange-500 text-white rounded-xl hover:bg-orange-600 transition-colors shadow-md hover:shadow-lg"
        >
          <Plus size={20} />
          방 만들기
        </Link>
      </div>

      <RoomSearchFilter onSearch={handleSearch} />

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Loader2 className="animate-spin text-orange-500" size={40} />
        </div>
      ) : rooms.length === 0 ? (
        <div className="text-center py-10 text-stone-500">
          검색 결과가 없습니다
        </div>
      ) : (
        <>
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
                  <span className={`flex items-center gap-1 text-sm font-medium ${
                    room.roomStatus === 'FULL' ? 'text-red-600' : 'text-emerald-600'
                  }`}>
                    <Users size={16} />
                    {room.currentParticipants}/{room.maxParticipants}
                  </span>
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

                <div className="flex items-center gap-2 text-sm text-stone-500 pt-2 border-t border-stone-100">
                  <User size={16} className="text-orange-500 flex-shrink-0" />
                  <span>{room.hostNickname}</span>
                </div>
              </Link>
            ))}
          </div>

          {loadingMore && (
            <div className="flex justify-center py-8">
              <Loader2 className="animate-spin text-orange-500" size={32} />
            </div>
          )}

          {!hasMore && rooms.length > 0 && (
            <div className="text-center py-8 text-stone-500">
              모든 방을 불러왔습니다
            </div>
          )}

          <div ref={observerRef} className="h-10" />
        </>
      )}
    </div>
  );
}
