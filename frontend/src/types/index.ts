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

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Review {
  id: number;
  reviewerId: number;
  reviewerNickname: string;
  roomId: number;
  roomRegion: string;
  gameTitle: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface UserReviewSummary {
  userId: number;
  nickname: string;
  averageRating: number | null;
  reviewCount: number;
  reviews: Review[];
}