import { useEffect, useState } from 'react';
import { User, Trophy, CheckCircle, XCircle, Star, MessageSquare } from 'lucide-react';
import client from '../api/client';
import { reviewApi } from '../api/review';
import { useAuthStore } from '../stores/authStore';
import type { ApiResponse, TrustScore, UserReviewSummary } from '../types';

export default function ProfilePage() {
  const currentUser = useAuthStore((state) => state.user);
  const [trustScore, setTrustScore] = useState<TrustScore | null>(null);
  const [reviewSummary, setReviewSummary] = useState<UserReviewSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [trustRes] = await Promise.all([
          client.get<ApiResponse<TrustScore>>('/trust-scores/me'),
        ]);
        setTrustScore(trustRes.data.data);

        // 리뷰 정보 조회
        if (currentUser) {
          const reviewData = await reviewApi.getUserReviews(currentUser.id);
          setReviewSummary(reviewData);
        }
      } catch (error) {
        console.error('데이터 조회 실패:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [currentUser]);

  if (loading) {
    return <div className="text-center py-10">로딩 중...</div>;
  }

  const gradeColor: Record<string, string> = {
    S: 'text-orange-700 bg-gradient-to-br from-orange-400 to-orange-500',
    A: 'text-orange-600 bg-gradient-to-br from-orange-300 to-orange-400',
    B: 'text-amber-600 bg-gradient-to-br from-amber-300 to-amber-400',
    C: 'text-yellow-700 bg-gradient-to-br from-yellow-300 to-yellow-400',
    D: 'text-orange-800 bg-gradient-to-br from-orange-200 to-orange-300',
    F: 'text-red-700 bg-gradient-to-br from-red-300 to-red-400',
  };

  const renderStars = (rating: number) => {
    return (
      <div className="flex gap-0.5">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            size={16}
            className={star <= rating ? 'fill-amber-400 text-amber-400' : 'text-gray-300'}
          />
        ))}
      </div>
    );
  };

  return (
    <div className="max-w-lg mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">내 프로필</h1>
        <p className="text-gray-600">나의 활동 기록과 신뢰도를 확인하세요</p>
      </div>

      {/* 평균 별점 카드 */}
      {reviewSummary && (
        <div className="bg-white rounded-2xl shadow-lg shadow-orange-100/50 p-6 mb-6 border border-orange-50">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-gradient-to-br from-amber-400 to-amber-500 rounded-xl flex items-center justify-center">
              <Star className="w-6 h-6 text-white fill-white" />
            </div>
            <h2 className="font-bold text-xl text-gray-900">매너 평점</h2>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-4xl font-bold text-amber-500">
              {reviewSummary.averageRating ? reviewSummary.averageRating.toFixed(1) : '-'}
            </div>
            <div>
              {reviewSummary.averageRating && renderStars(Math.round(reviewSummary.averageRating))}
              <p className="text-sm text-gray-500 mt-1">{reviewSummary.reviewCount}개의 리뷰</p>
            </div>
          </div>
        </div>
      )}

      {/* 신뢰도 카드 */}
      <div className="bg-white rounded-2xl shadow-lg shadow-orange-100/50 p-8 mb-6 border border-orange-50">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center">
            <Trophy className="w-6 h-6 text-white" />
          </div>
          <h2 className="font-bold text-xl text-gray-900">신뢰도</h2>
        </div>

        <div className="flex items-center justify-between mb-8">
          <div>
            <p className="text-5xl font-bold bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
              {trustScore?.score ?? 0}점
            </p>
            <p className="text-gray-500 text-sm mt-1 font-medium">Trust Score</p>
          </div>
          <div className={`text-5xl font-bold px-8 py-4 rounded-2xl shadow-lg text-white ${gradeColor[trustScore?.grade ?? 'B']}`}>
            {trustScore?.grade ?? 'B'}
          </div>
        </div>

        {/* 통계 */}
        <div className="grid grid-cols-3 gap-4 text-center">
          <div className="bg-gradient-to-br from-orange-50 to-amber-50 rounded-xl p-4 border border-orange-100">
            <div className="flex justify-center mb-2">
              <User className="w-5 h-5 text-orange-600" />
            </div>
            <p className="text-2xl font-bold text-gray-900">{trustScore?.totalParticipations ?? 0}</p>
            <p className="text-xs text-gray-600 font-medium mt-1">총 참여</p>
          </div>
          <div className="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-4 border border-green-100">
            <div className="flex justify-center mb-2">
              <CheckCircle className="w-5 h-5 text-green-600" />
            </div>
            <p className="text-2xl font-bold text-green-700">{trustScore?.attendedCount ?? 0}</p>
            <p className="text-xs text-gray-600 font-medium mt-1">출석</p>
          </div>
          <div className="bg-gradient-to-br from-red-50 to-rose-50 rounded-xl p-4 border border-red-100">
            <div className="flex justify-center mb-2">
              <XCircle className="w-5 h-5 text-red-600" />
            </div>
            <p className="text-2xl font-bold text-red-700">{trustScore?.noShowCount ?? 0}</p>
            <p className="text-xs text-gray-600 font-medium mt-1">노쇼</p>
          </div>
        </div>
      </div>

      {/* 받은 리뷰 목록 */}
      {reviewSummary && reviewSummary.reviews.length > 0 && (
        <div className="bg-white rounded-2xl shadow-lg shadow-orange-100/50 p-6 mb-6 border border-orange-50">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-blue-500 rounded-xl flex items-center justify-center">
              <MessageSquare className="w-6 h-6 text-white" />
            </div>
            <h2 className="font-bold text-xl text-gray-900">받은 리뷰</h2>
          </div>
          <div className="space-y-4">
            {reviewSummary.reviews.map((review) => (
              <div key={review.id} className="p-4 bg-gray-50 rounded-xl border border-gray-100">
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <p className="font-medium text-gray-900">{review.reviewerNickname}</p>
                    <p className="text-xs text-gray-500">{review.gameTitle} · {review.roomRegion}</p>
                  </div>
                  {renderStars(review.rating)}
                </div>
                {review.comment && (
                  <p className="text-sm text-gray-700 mt-2">{review.comment}</p>
                )}
                <p className="text-xs text-gray-400 mt-2">
                  {new Date(review.createdAt).toLocaleDateString('ko-KR')}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 등급 안내 */}
      <div className="bg-white rounded-2xl shadow-lg shadow-orange-100/50 p-8 border border-orange-50">
        <h2 className="font-bold text-xl text-gray-900 mb-6">등급 기준</h2>
        <div className="space-y-3 text-sm">
          <div className="flex items-center justify-between p-3 rounded-xl bg-gradient-to-r from-orange-50 to-amber-50">
            <span className={`px-4 py-2 rounded-lg font-bold text-white shadow-md ${gradeColor.S}`}>S</span>
            <span className="font-semibold text-gray-700">150점 이상</span>
          </div>
          <div className="flex items-center justify-between p-3 rounded-xl bg-gradient-to-r from-orange-50 to-amber-50">
            <span className={`px-4 py-2 rounded-lg font-bold text-white shadow-md ${gradeColor.A}`}>A</span>
            <span className="font-semibold text-gray-700">120 ~ 149점</span>
          </div>
          <div className="flex items-center justify-between p-3 rounded-xl bg-gradient-to-r from-orange-50 to-amber-50">
            <span className={`px-4 py-2 rounded-lg font-bold text-white shadow-md ${gradeColor.B}`}>B</span>
            <span className="font-semibold text-gray-700">100 ~ 119점</span>
          </div>
          <div className="flex items-center justify-between p-3 rounded-xl bg-gradient-to-r from-orange-50 to-amber-50">
            <span className={`px-4 py-2 rounded-lg font-bold text-white shadow-md ${gradeColor.C}`}>C</span>
            <span className="font-semibold text-gray-700">70 ~ 99점</span>
          </div>
          <div className="flex items-center justify-between p-3 rounded-xl bg-gradient-to-r from-orange-50 to-amber-50">
            <span className={`px-4 py-2 rounded-lg font-bold text-white shadow-md ${gradeColor.D}`}>D</span>
            <span className="font-semibold text-gray-700">40 ~ 69점</span>
          </div>
          <div className="flex items-center justify-between p-3 rounded-xl bg-gradient-to-r from-orange-50 to-amber-50">
            <span className={`px-4 py-2 rounded-lg font-bold text-white shadow-md ${gradeColor.F}`}>F</span>
            <span className="font-semibold text-gray-700">40점 미만</span>
          </div>
        </div>
        <div className="mt-6 p-4 bg-gradient-to-r from-orange-50 to-amber-50 rounded-xl border border-orange-100">
          <p className="text-sm text-gray-700 font-medium text-center">
            출석 <span className="text-green-600 font-bold">+10점</span> / 노쇼 <span className="text-red-600 font-bold">-30점</span>
          </p>
        </div>
      </div>
    </div>
  );
}
