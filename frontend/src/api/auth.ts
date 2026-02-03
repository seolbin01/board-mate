import client from './client';
import type { ApiResponse, User } from '../types';

// JWT 디코딩 함수
const parseJwt = (token: string): { sub: string; email: string; role: string } => {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join('')
  );
  return JSON.parse(jsonPayload);
};

export const authApi = {
  signup: async (email: string, password: string, nickname: string) => {
    const response = await client.post<ApiResponse<{ accessToken: string; refreshToken: string }>>('/auth/signup', { 
      email, password, nickname 
    });
    const { accessToken } = response.data.data;
    const payload = parseJwt(accessToken);
    
    const user: User = {
      id: Number(payload.sub),
      email: payload.email,
      nickname: nickname,  // 회원가입 시 입력한 닉네임 사용
    };
    
    return { accessToken, user };
  },

  login: async (email: string, password: string) => {
    const response = await client.post<ApiResponse<{ accessToken: string; refreshToken: string }>>('/auth/login', { 
      email, password 
    });
    const { accessToken } = response.data.data;
    const payload = parseJwt(accessToken);
    
    const user: User = {
      id: Number(payload.sub),
      email: payload.email,
      nickname: payload.email.split('@')[0],  // 임시로 이메일 앞부분 사용
    };
    
    return { accessToken, user };
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('user');
  },
};