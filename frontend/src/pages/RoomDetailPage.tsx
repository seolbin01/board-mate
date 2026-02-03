import { useEffect, useState, useRef } from 'react';
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
  Crown,
  Star,
  X,
  Send,
  MessageCircle,
  BookOpen
} from 'lucide-react';
import client from '../api/client';
import { reviewApi } from '../api/review';
import { chatApi } from '../api/chat';
import { useAuthStore } from '../stores/authStore';
import type { Room, ApiResponse, ChatMessage } from '../types';

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

  // 리뷰 관련 상태
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewTarget, setReviewTarget] = useState<Participant | null>(null);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewedUsers, setReviewedUsers] = useState<Set<number>>(new Set());

  // 채팅 관련 상태
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [sendingChat, setSendingChat] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);
  const stompClientRef = useRef<Client | null>(null);

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

  // 채팅 내역 조회
  const fetchChatHistory = async () => {
    try {
      const messages = await chatApi.getChatHistory(Number(id));
      setChatMessages(messages);
    } catch (error) {
      console.error('채팅 내역 조회 실패:', error);
    }
  };

  // 채팅 스크롤
  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [chatMessages]);

  // WebSocket 연결
  useEffect(() => {
    let isMounted = true;
    let stompClient: Client | null = null;

    fetchRoom();
    fetchChatHistory();

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';
    const token = localStorage.getItem('accessToken');

    stompClient = new Client({
      webSocketFactory: () => new SockJS(`${wsUrl}?token=${token}`),
      onConnect: () => {
        if (!isMounted || !stompClient) return;

        // 방 알림 구독
        stompClient.subscribe(`/topic/rooms/${id}`, (message) => {
          if (!isMounted) return;
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

        // 채팅 메시지 구독
        stompClient.subscribe(`/topic/chat/${id}`, (message) => {
          if (!isMounted) return;
          const chatMessage: ChatMessage = JSON.parse(message.body);
          setChatMessages((prev) => {
            // 중복 메시지 방지
            if (prev.some((m) => m.id === chatMessage.id)) {
              return prev;
            }
            return [...prev, chatMessage];
          });
        });

        stompClientRef.current = stompClient;
      },
    });

    stompClient.activate();

    return () => {
      isMounted = false;
      if (stompClient) {
        stompClient.deactivate();
      }
      stompClientRef.current = null;
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

  // 채팅 메시지 전송
  const handleSendChat = async () => {
    if (!chatInput.trim() || !user || sendingChat) return;

    setSendingChat(true);
    try {
      // WebSocket으로 전송 시도
      if (stompClientRef.current?.connected) {
        stompClientRef.current.publish({
          destination: `/app/chat/${id}`,
          body: JSON.stringify({ content: chatInput.trim() }),
        });
        setChatInput('');
      } else {
        // WebSocket 연결 안 되면 REST API 폴백
        await chatApi.sendMessage(Number(id), chatInput.trim());
        setChatInput('');
        fetchChatHistory();
      }
    } catch (error) {
      console.error('메시지 전송 실패:', error);
    } finally {
      setSendingChat(false);
    }
  };

  // 채팅 입력 엔터 키 처리 (한글 IME 중복 방지)
  const handleChatKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault();
      handleSendChat();
    }
  };

  // 리뷰 모달 열기
  const openReviewModal = (participant: Participant) => {
    setReviewTarget(participant);
    setReviewRating(5);
    setReviewComment('');
    setReviewModalOpen(true);
  };

  // 리뷰 제출
  const handleSubmitReview = async () => {
    if (!reviewTarget || !id) return;

    setSubmittingReview(true);
    try {
      await reviewApi.createReview(
        Number(id),
        reviewTarget.userId,
        reviewRating,
        reviewComment || undefined
      );
      alert('리뷰가 등록되었습니다!');
      setReviewedUsers(prev => new Set(prev).add(reviewTarget.userId));
      setReviewModalOpen(false);
    } catch (err: any) {
      alert(err.response?.data?.message || '리뷰 등록에 실패했습니다');
    } finally {
      setSubmittingReview(false);
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

  // 별점 렌더링
  const renderStars = (rating: number, interactive = false, onChange?: (r: number) => void) => {
    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            size={interactive ? 32 : 16}
            className={`${star <= rating ? 'fill-amber-400 text-amber-400' : 'text-gray-300'} ${interactive ? 'cursor-pointer hover:scale-110 transition-transform' : ''}`}
            onClick={() => interactive && onChange?.(star)}
          />
        ))}
      </div>
    );
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

          {/* 룰마스터 버튼 */}
          {room.bggId && (
            <div className="pt-2">
              <button
                onClick={() => navigate(`/rulemaster?bggId=${room.bggId}`)}
                className="flex items-center gap-2 px-4 py-2 bg-purple-50 text-purple-700 rounded-xl hover:bg-purple-100 transition-colors text-sm font-medium"
              >
                <BookOpen size={16} />
                룰마스터에게 물어보기
              </button>
            </div>
          )}

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
                {p.userId === user?.id && (
                  <span className="text-xs bg-orange-100 text-orange-600 px-2 py-1 rounded-lg font-medium">
                    나
                  </span>
                )}
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

              <div className="flex gap-2">
                {attendanceMode && p.userId !== user?.id && (
                  <>
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
                  </>
                )}

                {/* 리뷰 버튼 - 게임 종료 후 자신이 아닌 참가자에게만 표시 */}
                {room.roomStatus === 'CLOSED' &&
                 isParticipant &&
                 p.userId !== user?.id &&
                 !reviewedUsers.has(p.userId) && (
                  <button
                    onClick={() => openReviewModal(p)}
                    className="flex items-center gap-1 text-xs px-3 py-2 bg-amber-100 text-amber-700 rounded-xl hover:bg-amber-200 font-medium transition-colors"
                  >
                    <Star size={14} />
                    리뷰
                  </button>
                )}
                {reviewedUsers.has(p.userId) && (
                  <span className="flex items-center gap-1 text-xs px-3 py-2 bg-stone-100 text-stone-500 rounded-xl font-medium">
                    <CheckCircle size={14} />
                    리뷰 완료
                  </span>
                )}
              </div>
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

      {/* 채팅 섹션 - 참가자만 */}
      {isParticipant && room.roomStatus !== 'CLOSED' && (
        <div className="bg-white rounded-xl shadow-md border border-stone-200 p-6 mb-4">
          <h2 className="font-bold text-lg flex items-center gap-2 text-stone-800 mb-4">
            <MessageCircle size={20} className="text-orange-600" />
            채팅
          </h2>

          {/* 메시지 목록 */}
          <div className="h-64 overflow-y-auto bg-stone-50 rounded-xl p-4 mb-4 space-y-3">
            {chatMessages.length === 0 ? (
              <p className="text-center text-stone-400 text-sm py-8">
                아직 메시지가 없습니다. 첫 메시지를 보내보세요!
              </p>
            ) : (
              chatMessages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex flex-col ${msg.senderId === user?.id ? 'items-end' : 'items-start'}`}
                >
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs text-stone-500">{msg.senderNickname}</span>
                    <span className="text-xs text-stone-400">
                      {new Date(msg.createdAt).toLocaleTimeString('ko-KR', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  </div>
                  <div
                    className={`max-w-[75%] px-4 py-2 rounded-2xl ${
                      msg.senderId === user?.id
                        ? 'bg-orange-500 text-white rounded-br-md'
                        : 'bg-white border border-stone-200 text-stone-800 rounded-bl-md'
                    }`}
                  >
                    <p className="text-sm whitespace-pre-wrap break-words">{msg.content}</p>
                  </div>
                </div>
              ))
            )}
            <div ref={chatEndRef} />
          </div>

          {/* 입력창 */}
          <div className="flex gap-2">
            <input
              type="text"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onKeyDown={handleChatKeyDown}
              placeholder="메시지를 입력하세요..."
              className="flex-1 px-4 py-3 border border-stone-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
              maxLength={500}
            />
            <button
              onClick={handleSendChat}
              disabled={!chatInput.trim() || sendingChat}
              className="px-4 py-3 bg-orange-600 text-white rounded-xl hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <Send size={20} />
            </button>
          </div>
        </div>
      )}

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

        {isParticipant && !isHost && room.roomStatus !== 'CLOSED' && (
          <button
            onClick={handleLeave}
            className="flex-1 py-3 bg-stone-200 text-stone-700 rounded-xl hover:bg-stone-300 font-medium transition-colors flex items-center justify-center gap-2 shadow-md"
          >
            <LogOut size={18} />
            나가기
          </button>
        )}

        {isHost && room.roomStatus !== 'CLOSED' && (
          <button
            onClick={handleDelete}
            className="flex-1 py-3 bg-rose-500 text-white rounded-xl hover:bg-rose-600 font-medium transition-colors flex items-center justify-center gap-2 shadow-md"
          >
            <Trash2 size={18} />
            방 삭제
          </button>
        )}
      </div>

      {/* 리뷰 모달 */}
      {reviewModalOpen && reviewTarget && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold text-gray-900">리뷰 작성</h3>
              <button
                onClick={() => setReviewModalOpen(false)}
                className="p-2 hover:bg-gray-100 rounded-full transition-colors"
              >
                <X size={20} className="text-gray-500" />
              </button>
            </div>

            <div className="mb-6">
              <p className="text-gray-600 mb-2">
                <span className="font-semibold text-gray-900">{reviewTarget.nickname}</span>님에 대한 리뷰
              </p>
              <p className="text-sm text-gray-500">
                {room.gameTitle} · {room.region}
              </p>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-3">별점</label>
              <div className="flex justify-center">
                {renderStars(reviewRating, true, setReviewRating)}
              </div>
              <p className="text-center text-sm text-gray-500 mt-2">
                {reviewRating === 5 ? '최고예요!' :
                 reviewRating === 4 ? '좋아요' :
                 reviewRating === 3 ? '보통이에요' :
                 reviewRating === 2 ? '별로예요' : '나빠요'}
              </p>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                코멘트 (선택)
              </label>
              <textarea
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
                placeholder="함께한 경험을 공유해주세요..."
                className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 resize-none"
                rows={3}
                maxLength={500}
              />
              <p className="text-xs text-gray-400 mt-1 text-right">
                {reviewComment.length}/500
              </p>
            </div>

            <button
              onClick={handleSubmitReview}
              disabled={submittingReview}
              className="w-full py-3 bg-orange-600 text-white rounded-xl hover:bg-orange-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium transition-colors flex items-center justify-center gap-2"
            >
              <Send size={18} />
              {submittingReview ? '등록 중...' : '리뷰 등록'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
