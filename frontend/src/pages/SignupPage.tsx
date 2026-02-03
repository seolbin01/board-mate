import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, UserPlus, Dice6 } from 'lucide-react';
import { authApi } from '../api/auth';
import { useAuthStore } from '../stores/authStore';

export default function SignupPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const setUser = useAuthStore((state) => state.setUser);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
  
    try {
      const { accessToken, user } = await authApi.signup(email, password, nickname);
      setUser(user, accessToken);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-orange-50 via-amber-50 to-orange-100">
      <div className="max-w-md w-full p-8 bg-white rounded-2xl shadow-xl shadow-orange-100/50">
        <div className="flex flex-col items-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl flex items-center justify-center mb-4 shadow-lg shadow-orange-200">
            <Dice6 className="w-9 h-9 text-white" strokeWidth={2.5} />
          </div>
          <h1 className="text-3xl font-bold text-gray-900">회원가입</h1>
          <p className="text-sm text-gray-500 mt-1">함께 즐길 메이트를 찾아보세요</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-100 text-red-700 rounded-xl">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              이메일
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Mail className="h-5 w-5 text-orange-400" />
              </div>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                placeholder="your@email.com"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              닉네임
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <User className="h-5 w-5 text-orange-400" />
              </div>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                placeholder="게임에서 사용할 이름"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              비밀번호
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Lock className="h-5 w-5 text-orange-400" />
              </div>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all"
                placeholder="8자 이상"
                minLength={8}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-semibold rounded-xl hover:from-orange-600 hover:to-orange-700 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-orange-200 transition-all flex items-center justify-center gap-2"
          >
            <UserPlus className="w-5 h-5" />
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-600">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="text-orange-600 hover:text-orange-700 font-semibold hover:underline">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
}