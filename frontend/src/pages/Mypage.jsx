import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

function MyPage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/mypage/profile')
      .then((res) => {
        setProfile(res.data.data); // ApiResponse 래핑된 구조!
        setLoading(false);
      })
      .catch((err) => {
        console.error('프로필 조회 실패:', err);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>로딩 중...</div>;

  if (!profile) return <div>데이터가 없어요! 😢</div>;

  return (
    <div style={{ padding: '2rem' }}>
      <h2>🐶 마이페이지</h2>
      <p><strong>이메일:</strong> {profile.email}</p>
      <p><strong>이름:</strong> {profile.name}</p>
      <p><strong>닉네임:</strong> {profile.nickname}</p>
      <p><strong>전화번호:</strong> {profile.phone}</p>

      <button onClick={() => navigate('/mypage/dogs')}>나의 강아지 보기</button>

    </div>
  );
}

export default MyPage;