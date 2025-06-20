import React, { useEffect, useState } from 'react';
import api from '../api/axios';
// import defaultProfile from '../assets/default-profile.png'; // 기본 이미지가 있다면

function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/mypage/profile')
      .then((res) => {
        setProfile(res.data.data);
      })
      .catch((err) => {
        console.error('프로필 조회 실패:', err);
        alert('불러오기 실패 ㅠㅠ');
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  if (loading) return <p>로딩 중...</p>;
  if (!profile) return <p>데이터가 없어요! 😢</p>;

  return (
    <div className="max-w-3xl mx-auto bg-white p-8 rounded shadow">
      <h2 className="text-2xl font-bold mb-6 text-blue-500">내 프로필</h2>

      {/* 프로필 이미지 */}
      <div className="flex items-center justify-center mb-6 relative">
        {/* <img
          src={defaultProfile}
          alt="프로필 이미지"
          className="w-32 h-32 rounded-full border object-cover"
        /> */}
        <button className="absolute bottom-0 text-sm mt-2 border px-2 py-1 rounded">
          📷 사진 편집하기
        </button>
      </div>

      {/* 정보 리스트 */}
      <div className="space-y-4">
        <InfoRow label="이름" value={profile.name} />
        <InfoRow label="닉네임" value={profile.nickname} />
        <InfoRow label="전화번호" value={profile.phone} />
        <InfoRow label="이메일" value={profile.email} />
      </div>
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div>
      <label className="text-sm text-gray-500">{label}</label>
      <div className="text-lg font-medium">{value}</div>
    </div>
  );
}

export default Profile;