export interface User {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface Room {
  id: number;
  hostNickname: string;
  gameTitle: string;
  region: string;
  cafeName?: string;
  gameDate: string;
  maxParticipants: number;
  currentParticipants: number;
  description?: string;
  roomStatus: 'WAITING' | 'FULL' | 'CLOSED';
}

export interface TrustScore {
  userId: number;
  score: number;
  grade: string;
  totalParticipations: number;
  attendedCount: number;
  noShowCount: number;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface Game {
  id: number;
  title: string;
  minPlayers: number;
  maxPlayers: number;
}

export interface RoomSearchParams {
  region?: string;
  gameId?: number;
  date?: string;
}