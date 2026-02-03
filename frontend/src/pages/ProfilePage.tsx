import { useEffect, useState } from 'react';
import client from '../api/client';
import type { ApiResponse, TrustScore } from '../types';

export default function ProfilePage() {
  const [trustScore, setTrustScore] = useState<TrustScore | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTrustScore = async () => {
      try {
        const response = await client.get<ApiResponse<TrustScore>>('/trust-scores/me');
        setTrustScore(response.data.data);
      } catch (error) {
        console.error('신뢰도 조회 실패:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchTrustScore();
  }, []);

  if (loading) {
    return <div className="text-center py-10">로딩 중...</div>;
  }

  const gradeColor: Record<string, string> = {
    S: 'text-purple-600 bg-purple-100',
    A: 'text-blue-600 bg-blue-100',
    B: 'text-green-600 bg-green-100',
    C: 'text-yellow-600 bg-yellow-100',
    D: 'text-orange-600 bg-orange-100',
    F: 'text-red-600 bg-red-100',
  };

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-2xl font-bold mb-6">내 프로필</h1>

      {/* 신뢰도 카드 */}
      <div className="bg-white rounded-lg shadow p-6 mb-4">
        <h2 className="font-bold text-lg mb-4">🏆 신뢰도</h2>
        
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="text-4xl font-bold">{trustScore?.score ?? 0}점</p>
            <p className="text-gray-500 text-sm">Trust Score</p>
          </div>
          <div className={`text-4xl font-bold px-6 py-3 rounded-lg ${gradeColor[trustScore?.grade ?? 'B']}`}>
            {trustScore?.grade ?? 'B'}
          </div>
        </div>

        {/* 통계 */}
        <div className="grid grid-cols-3 gap-4 text-center">
          <div className="bg-gray-50 rounded-lg p-3">
            <p className="text-2xl font-bold">{trustScore?.totalParticipations ?? 0}</p>
            <p className="text-xs text-gray-500">총 참여</p>
          </div>
          <div className="bg-green-50 rounded-lg p-3">
            <p className="text-2xl font-bold text-green-600">{trustScore?.attendedCount ?? 0}</p>
            <p className="text-xs text-gray-500">출석</p>
          </div>
          <div className="bg-red-50 rounded-lg p-3">
            <p className="text-2xl font-bold text-red-600">{trustScore?.noShowCount ?? 0}</p>
            <p className="text-xs text-gray-500">노쇼</p>
          </div>
        </div>
      </div>

      {/* 등급 안내 */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="font-bold text-lg mb-4">📊 등급 기준</h2>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between"><span className={`px-2 rounded ${gradeColor.S}`}>S</span><span>150점 이상</span></div>
          <div className="flex justify-between"><span className={`px-2 rounded ${gradeColor.A}`}>A</span><span>120 ~ 149점</span></div>
          <div className="flex justify-between"><span className={`px-2 rounded ${gradeColor.B}`}>B</span><span>100 ~ 119점</span></div>
          <div className="flex justify-between"><span className={`px-2 rounded ${gradeColor.C}`}>C</span><span>70 ~ 99점</span></div>
          <div className="flex justify-between"><span className={`px-2 rounded ${gradeColor.D}`}>D</span><span>40 ~ 69점</span></div>
          <div className="flex justify-between"><span className={`px-2 rounded ${gradeColor.F}`}>F</span><span>40점 미만</span></div>
        </div>
        <p className="text-xs text-gray-400 mt-4">출석 +10점 / 노쇼 -30점</p>
      </div>
    </div>
  );
}