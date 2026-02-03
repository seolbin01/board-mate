import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import client from '../api/client';
import { useAuthStore } from '../stores/authStore';
import type { Room, ApiResponse } from '../types';

interface Participant {
  id: number;
  userId: number;
  nickname: string;
  attendanceStatus: string;
}

interface RoomNotification {
  type: string;
  roomId: number;
  userId: number;
  nickname: string;
  currentParticipants: number;
  maxParticipants: number;
  message: string;
}

export default function RoomDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  
  const [room, setRoom] = useState<Room | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [notifications, setNotifications] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [joining, setJoining] = useState(false);

  const isHost = room?.hostNickname === user?.nickname;
  const isParticipant = participants.some(p => p.userId === user?.id);

  const [attendanceMode, setAttendanceMode] = useState(false);
  const [attendances, setAttendances] = useState<Record<number, string>>({});

  // 방 정보 & 참가자 조회
  const fetchRoom = async () => {
    try {
      const [roomRes, participantsRes] = await Promise.all([
        client.get<ApiResponse<Room>>(`/rooms/${id}`),
        client.get<ApiResponse<Participant[]>>(`/rooms/${id}/participants`),
      ]);
      setRoom(roomRes.data.data);
      setParticipants(participantsRes.data.data);
    } catch (error) {
      console.error('방 정보 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  // WebSocket 연결
  useEffect(() => {
    fetchRoom();

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      onConnect: () => {
        stompClient.subscribe(`/topic/rooms/${id}`, (message) => {
          const notification: RoomNotification = JSON.parse(message.body);
          setNotifications((prev) => [...prev, notification.message]);
          
          // 참가자 수 업데이트
          setRoom((prev) => prev ? {
            ...prev,
            currentParticipants: notification.currentParticipants,
          } : null);
          
          // 참가자 목록 새로고침
          fetchRoom();
        });
      },
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [id]);

  // 참가하기
  const handleJoin = async () => {
    setJoining(true);
    try {
      await client.post(`/rooms/${id}/participants`);
      fetchRoom();
    } catch (err: any) {
      alert(err.response?.data?.message || '참가에 실패했습니다');
    } finally {
      setJoining(false);
    }
  };

  // 나가기
  const handleLeave = async () => {
    if (!confirm('정말 나가시겠습니까?')) return;
    try {
      await client.delete(`/rooms/${id}/participants`);
      fetchRoom();
    } catch (err: any) {
      alert(err.response?.data?.message || '나가기에 실패했습니다');
    }
  };

  // 방 삭제 (방장만)
  const handleDelete = async () => {
    if (!confirm('정말 방을 삭제하시겠습니까?')) return;
    try {
      await client.delete(`/rooms/${id}`);
      navigate('/');
    } catch (err: any) {
      alert(err.response?.data?.message || '삭제에 실패했습니다');
    }
  };

  if (loading) {
    return <div className="text-center py-10">로딩 중...</div>;
  }

  if (!room) {
    return <div className="text-center py-10">방을 찾을 수 없습니다</div>;
  }

  // 출석 상태 변경
  const handleAttendanceChange = (userId: number, status: string) => {
    setAttendances((prev) => ({ ...prev, [userId]: status }));
  };

  // 출석 체크 제출
  const handleAttendanceSubmit = async () => {
    const attendanceList = Object.entries(attendances).map(([userId, status]) => ({
      userId: Number(userId),
      status,
    }));

    if (attendanceList.length === 0) {
      alert('출석 상태를 선택해주세요');
      return;
    }

    try {
      await client.post(`/rooms/${id}/participants/attendance`, {
        attendances: attendanceList,
      });
      alert('출석 체크가 완료되었습니다');
      setAttendanceMode(false);
      fetchRoom();
    } catch (err: any) {
      alert(err.response?.data?.message || '출석 체크에 실패했습니다');
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      {/* 헤더 */}
      <div className="bg-white rounded-lg shadow p-6 mb-4">
        <div className="flex justify-between items-start mb-4">
          <span className="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm">
            {room.gameTitle}
          </span>
          <span className={`px-3 py-1 rounded-full text-sm ${
            room.roomStatus === 'WAITING' ? 'bg-green-100 text-green-700' :
            room.roomStatus === 'FULL' ? 'bg-red-100 text-red-700' :
            'bg-gray-100 text-gray-700'
          }`}>
            {room.roomStatus === 'WAITING' ? '모집중' : 
             room.roomStatus === 'FULL' ? '마감' : '종료'}
          </span>
        </div>

        <h1 className="text-xl font-bold mb-2">{room.region}</h1>
        {room.cafeName && <p className="text-gray-600 mb-2">{room.cafeName}</p>}
        
        <div className="text-gray-600 mb-4">
          📅 {new Date(room.gameDate).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
          })}
        </div>

        {room.description && (
          <p className="text-gray-700 mb-4">{room.description}</p>
        )}

        <div className="text-sm text-gray-500">
          방장: {room.hostNickname}
        </div>
      </div>

      {/* 참가자 목록 */}
      <div className="bg-white rounded-lg shadow p-6 mb-4">
        <div className="flex justify-between items-center mb-4">
          <h2 className="font-bold">
            참가자 ({room.currentParticipants}/{room.maxParticipants})
          </h2>
          {isHost && room.roomStatus !== 'CLOSED' && (
            <button
              onClick={() => setAttendanceMode(!attendanceMode)}
              className="text-sm px-3 py-1 bg-yellow-100 text-yellow-700 rounded hover:bg-yellow-200"
            >
              {attendanceMode ? '취소' : '출석 체크'}
            </button>
          )}
        </div>

        <div className="space-y-2">
          {participants.map((p) => (
            <div key={p.id} className="flex justify-between items-center py-2 border-b">
              <div className="flex items-center gap-2">
                <span>{p.nickname}</span>
                {p.userId === participants[0]?.userId && (
                  <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded">
                    방장
                  </span>
                )}
                {p.attendanceStatus !== 'PENDING' && (
                  <span className={`text-xs px-2 py-1 rounded ${
                    p.attendanceStatus === 'ATTENDED' 
                      ? 'bg-green-100 text-green-700' 
                      : 'bg-red-100 text-red-700'
                  }`}>
                    {p.attendanceStatus === 'ATTENDED' ? '출석' : '노쇼'}
                  </span>
                )}
              </div>

              {attendanceMode && p.userId !== user?.id && (
                <div className="flex gap-2">
                  <button
                    onClick={() => handleAttendanceChange(p.userId, 'ATTENDED')}
                    className={`text-xs px-3 py-1 rounded ${
                      attendances[p.userId] === 'ATTENDED'
                        ? 'bg-green-500 text-white'
                        : 'bg-gray-100 hover:bg-green-100'
                    }`}
                  >
                    출석
                  </button>
                  <button
                    onClick={() => handleAttendanceChange(p.userId, 'NO_SHOW')}
                    className={`text-xs px-3 py-1 rounded ${
                      attendances[p.userId] === 'NO_SHOW'
                        ? 'bg-red-500 text-white'
                        : 'bg-gray-100 hover:bg-red-100'
                    }`}
                  >
                    노쇼
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>

        {attendanceMode && (
          <button
            onClick={handleAttendanceSubmit}
            className="w-full mt-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
          >
            출석 체크 완료
          </button>
        )}
      </div>

      {/* 실시간 알림 */}
      {notifications.length > 0 && (
        <div className="bg-blue-50 rounded-lg p-4 mb-4">
          <h3 className="font-bold text-blue-700 mb-2">실시간 알림</h3>
          {notifications.slice(-5).map((msg, i) => (
            <p key={i} className="text-sm text-blue-600">{msg}</p>
          ))}
        </div>
      )}

      {/* 액션 버튼 */}
      <div className="flex gap-2">
        {!isParticipant && room.roomStatus === 'WAITING' && (
          <button
            onClick={handleJoin}
            disabled={joining}
            className="flex-1 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50"
          >
            {joining ? '참가 중...' : '참가하기'}
          </button>
        )}

        {isParticipant && !isHost && (
          <button
            onClick={handleLeave}
            className="flex-1 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
          >
            나가기
          </button>
        )}

        {isHost && (
          <button
            onClick={handleDelete}
            className="flex-1 py-3 bg-red-500 text-white rounded-lg hover:bg-red-600"
          >
            방 삭제
          </button>
        )}
      </div>
    </div>
  );
}