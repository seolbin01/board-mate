import { useState } from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { Dice6, Users, User, LogOut, Wine, X } from 'lucide-react';
import { useAuthStore } from '../stores/authStore';
import SommelierChat from './SommelierChat';

export default function Layout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [isChatOpen, setIsChatOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-amber-50/50">
      <nav className="bg-white shadow-sm border-b border-amber-100">
        <div className="max-w-6xl mx-auto px-4 py-3 flex justify-between items-center">
          <Link to="/" className="flex items-center gap-2 text-xl font-bold text-orange-600 hover:text-orange-700 transition-colors">
            <Dice6 className="w-6 h-6" />
            <span className="tracking-tight">BoardMate</span>
          </Link>
          <div className="flex items-center gap-6">
            <Link
              to="/my-rooms"
              className="flex items-center gap-2 text-gray-700 hover:text-orange-600 transition-colors font-medium"
            >
              <Users className="w-4 h-4" />
              <span>내 모임</span>
            </Link>
            <Link
              to="/profile"
              className="flex items-center gap-2 text-gray-700 hover:text-orange-600 transition-colors font-medium"
            >
              <User className="w-4 h-4" />
              <span>{user?.nickname || '사용자'}</span>
            </Link>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 px-4 py-2 text-sm text-gray-600 hover:text-gray-900 transition-colors"
            >
              <LogOut className="w-4 h-4" />
              <span>로그아웃</span>
            </button>
          </div>
        </div>
      </nav>
      <main className="max-w-6xl mx-auto px-4 py-6">
        <Outlet />
      </main>

      {/* 소믈리에 채팅 */}
      <SommelierChat isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />

      {/* 소믈리에 FAB */}
      <button
        onClick={() => setIsChatOpen(!isChatOpen)}
        className={`fixed bottom-6 right-6 w-14 h-14 ${isChatOpen ? 'bg-stone-500 hover:bg-stone-600' : 'bg-purple-500 hover:bg-purple-600'} text-white rounded-full shadow-lg hover:shadow-xl transition-all flex items-center justify-center group z-50`}
        title="보드게임 소믈리에"
      >
        {isChatOpen ? <X className="w-6 h-6" /> : <Wine className="w-6 h-6" />}
        {!isChatOpen && (
          <span className="absolute right-full mr-3 px-3 py-1.5 bg-gray-800 text-white text-sm rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
            게임 추천받기
          </span>
        )}
      </button>
    </div>
  );
}
