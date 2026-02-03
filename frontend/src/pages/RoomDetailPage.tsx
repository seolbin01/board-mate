import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {
  MapPin,
  Building2,
  Calendar,
  Users,
  User,
  Clock,
  FileText,
  LogIn,
  LogOut,
  Trash2,
  CheckCircle,
  XCircle,
  Gamepad2,
  Crown
} from 'lucide-react';
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

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';
    const stompClient = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
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
      <div className="bg-white rounded-xl shadow-md border border-stone-200 p-6 mb-4">
        <div className="flex justify-between items-start mb-6">
          <div className="flex items-center gap-2 px-4 py-2 bg-orange-100 text-orange-700 rounded-xl text-sm font-medium">
            <Gamepad2 size={16} />
            {room.gameTitle}
          </div>
          <span className={`px-4 py-2 rounded-xl text-sm font-medium ${
            room.roomStatus === 'WAITING' ? 'bg-emerald-100 text-emerald-700' :
            room.roomStatus === 'FULL' ? 'bg-rose-100 text-rose-700' :
            'bg-stone-100 text-stone-700'
          }`}>
            {room.roomStatus === 'WAITING' ? '모집중' :
             room.roomStatus === 'FULL' ? '마감' : '종료'}
          </span>
        </div>

        <div className="space-y-4">
          <div className="flex items-center gap-3 text-stone-700">
            <MapPin size={20} className="text-orange-600" />
            <h1 className="text-2xl font-bold">{room.region}</h1>
          </div>

          {room.cafeName && (
            <div className="flex items-center gap-3 text-stone-600">
              <Building2 size={18} className="text-orange-500" />
              <p className="text-base">{room.cafeName}</p>
            </div>
          )}

          <div className="flex items-center gap-3 text-stone-600">
            <Calendar size={18} className="text-orange-500" />
            <span>{new Date(room.gameDate).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}</span>
          </div>

          <div className="flex items-center gap-3 text-stone-600">
            <Clock size={18} className="text-orange-500" />
            <span>{new Date(room.gameDate).toLocaleTimeString('ko-KR', {
              hour: '2-digit',
              minute: '2-digit',
            })}</span>
          </div>

          <div className="flex items-center gap-3 text-stone-600">
            <Users size={18} className="text-orange-500" />
            <span>{room.currentParticipants} / {room.maxParticipants}명</span>
          </div>

          {room.description && (
            <div className="pt-4 border-t border-stone-200">
              <div className="flex items-start gap-3 text-stone-700">
                <FileText size={18} className="text-orange-500 mt-1" />
                <p className="text-base leading-relaxed">{room.description}</p>
              </div>
            </div>
          )}

          <div className="flex items-center gap-3 text-sm text-stone-500 pt-2">
            <Crown size={16} className="text-amber-500" />
            <span>방장: {room.hostNickname}</span>
          </div>
        </div>
      </div>

      {/* 참가자 목록 */}
      <div className="bg-white rounded-xl shadow-md border border-stone-200 p-6 mb-4">
        <div className="flex justify-between items-center mb-6">
          <h2 className="font-bold text-lg flex items-center gap-2 text-stone-800">
            <Users size={20} className="text-orange-600" />
            참가자 ({room.currentParticipants}/{room.maxParticipants})
          </h2>
          {isHost && room.roomStatus !== 'CLOSED' && (
            <button
              onClick={() => setAttendanceMode(!attendanceMode)}
              className="text-sm px-4 py-2 bg-amber-100 text-amber-700 rounded-xl hover:bg-amber-200 font-medium transition-colors"
            >
              {attendanceMode ? '취소' : '출석 체크'}
            </button>
          )}
        </div>

        <div className="space-y-3">
          {participants.map((p) => (
            <div key={p.id} className="flex justify-between items-center py-3 px-4 bg-stone-50 rounded-xl border border-stone-200 hover:border-orange-300 transition-colors">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 bg-orange-200 rounded-full flex items-center justify-center">
                  <User size={16} className="text-orange-700" />
                </div>
                <span className="font-medium text-stone-800">{p.nickname}</span>
                {p.userId === participants[0]?.userId && (
                  <span className="flex items-center gap-1 text-xs bg-amber-100 text-amber-700 px-3 py-1 rounded-xl font-medium">
                    <Crown size={12} />
                    방장
                  </span>
                )}
                {p.attendanceStatus !== 'PENDING' && (
                  <span className={`flex items-center gap-1 text-xs px-3 py-1 rounded-xl font-medium ${
                    p.attendanceStatus === 'ATTENDED'
                      ? 'bg-emerald-100 text-emerald-700'
                      : 'bg-rose-100 text-rose-700'
                  }`}>
                    {p.attendanceStatus === 'ATTENDED' ? (
                      <>
                        <CheckCircle size={12} />
                        출석
                      </>
                    ) : (
                      <>
                        <XCircle size={12} />
                        노쇼
                      </>
                    )}
                  </span>
                )}
              </div>

              {attendanceMode && p.userId !== user?.id && (
                <div className="flex gap-2">
                  <button
                    onClick={() => handleAttendanceChange(p.userId, 'ATTENDED')}
                    className={`flex items-center gap-1 text-xs px-3 py-2 rounded-xl font-medium transition-colors ${
                      attendances[p.userId] === 'ATTENDED'
                        ? 'bg-emerald-500 text-white'
                        : 'bg-stone-100 hover:bg-emerald-100 text-stone-700'
                    }`}
                  >
                    <CheckCircle size={14} />
                    출석
                  </button>
                  <button
                    onClick={() => handleAttendanceChange(p.userId, 'NO_SHOW')}
                    className={`flex items-center gap-1 text-xs px-3 py-2 rounded-xl font-medium transition-colors ${
                      attendances[p.userId] === 'NO_SHOW'
                        ? 'bg-rose-500 text-white'
                        : 'bg-stone-100 hover:bg-rose-100 text-stone-700'
                    }`}
                  >
                    <XCircle size={14} />
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
            className="w-full mt-6 py-3 bg-orange-600 text-white rounded-xl hover:bg-orange-700 font-medium transition-colors flex items-center justify-center gap-2"
          >
            <CheckCircle size={18} />
            출석 체크 완료
          </button>
        )}
      </div>

      {/* 실시간 알림 */}
      {notifications.length > 0 && (
        <div className="bg-orange-50 rounded-xl shadow-md border border-orange-200 p-5 mb-4">
          <h3 className="font-bold text-orange-800 mb-3 flex items-center gap-2">
            <div className="w-2 h-2 bg-orange-500 rounded-full animate-pulse"></div>
            실시간 알림
          </h3>
          <div className="space-y-2">
            {notifications.slice(-5).map((msg, i) => (
              <p key={i} className="text-sm text-orange-700 pl-4 border-l-2 border-orange-300">{msg}</p>
            ))}
          </div>
        </div>
      )}

      {/* 액션 버튼 */}
      <div className="flex gap-3">
        {!isParticipant && room.roomStatus === 'WAITING' && (
          <button
            onClick={handleJoin}
            disabled={joining}
            className="flex-1 py-3 bg-orange-600 text-white rounded-xl hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium transition-colors flex items-center justify-center gap-2 shadow-md"
          >
            <LogIn size={18} />
            {joining ? '참가 중...' : '참가하기'}
          </button>
        )}

        {isParticipant && !isHost && (
          <button
            onClick={handleLeave}
            className="flex-1 py-3 bg-stone-200 text-stone-700 rounded-xl hover:bg-stone-300 font-medium transition-colors flex items-center justify-center gap-2 shadow-md"
          >
            <LogOut size={18} />
            나가기
          </button>
        )}

        {isHost && (
          <button
            onClick={handleDelete}
            className="flex-1 py-3 bg-rose-500 text-white rounded-xl hover:bg-rose-600 font-medium transition-colors flex items-center justify-center gap-2 shadow-md"
          >
            <Trash2 size={18} />
            방 삭제
          </button>
        )}
      </div>
    </div>
  );
}