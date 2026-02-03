import client from './client';
import type { ApiResponse, Review, UserReviewSummary } from '../types';

export const reviewApi = {
  createReview: async (roomId: number, revieweeId: number, rating: number, comment?: string) => {
    const response = await client.post<ApiResponse<Review>>('/reviews', {
      roomId,
      revieweeId,
      rating,
      comment,
    });
    return response.data.data;
  },

  getUserReviews: async (userId: number) => {
    const response = await client.get<ApiResponse<UserReviewSummary>>(`/users/${userId}/reviews`);
    return response.data.data;
  },
};
