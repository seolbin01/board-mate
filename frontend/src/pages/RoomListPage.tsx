import { useEffect, useState, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
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
        <>
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

          {loadingMore && (
            <div className="text-center py-8">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
            </div>
          )}

          {!hasMore && rooms.length > 0 && (
            <div className="text-center py-8 text-gray-500">
              모든 방을 불러왔습니다
            </div>
          )}

          <div ref={observerRef} className="h-10" />
        </>
      )}
    </div>
  );
}
