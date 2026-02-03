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
  bggId?: number; // BoardGameGeek ID
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

export interface ChatMessage {
  id: number;
  roomId: number;
  senderId: number;
  senderNickname: string;
  content: string;
  createdAt: string;
}

// 룰마스터 관련 타입
export interface BggGameSearchResult {
  bggId: number;
  name: string;
  yearPublished: number;
  thumbnailUrl?: string;
}

export interface BggGameDetail {
  bggId: number;
  name: string;
  nameKorean?: string;
  yearPublished: number;
  description: string;
  minPlayers: number;
  maxPlayers: number;
  playingTime: number;
  minPlayTime: number;
  maxPlayTime: number;
  mechanics: string[];
  categories: string[];
  imageUrl?: string;
  thumbnailUrl?: string;
  averageRating: number;
  weight: number;
}

export interface RuleMasterMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

export interface ConversationSession {
  bggId: number;
  gameName: string;
  messages: RuleMasterMessage[];
  expiresAt?: string;
}

export interface ChatStreamEvent {
  type: 'content' | 'done' | 'error';
  content?: string;
  messageId?: string;
  error?: {
    code: string;
    message: string;
    retryable: boolean;
  };
}