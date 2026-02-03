import { create } from 'zustand';
import type { User } from '../types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  setUser: (user: User, token: string) => void;
  logout: () => void;
}

// localStorage에서 user 복원 (안전하게)
const getStoredUser = (): User | null => {
  try {
    const stored = localStorage.getItem('user');
    if (stored && stored !== 'undefined') {
      return JSON.parse(stored);
    }
  } catch {
    // 파싱 실패시 무시
  }
  return null;
};

export const useAuthStore = create<AuthState>((set) => ({
  user: getStoredUser(),
  isAuthenticated: !!localStorage.getItem('accessToken'),
  
  setUser: (user, token) => {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('user', JSON.stringify(user));
    set({ user, isAuthenticated: true });
  },
  
  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
    set({ user: null, isAuthenticated: false });
  },
}));