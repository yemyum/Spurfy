// src/layouts/RootLayout.jsx (여기에 이 코드를 복붙해줘!)

import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import api from '../api/axios';
// ⭐ 만약 AuthContext를 만들었다면 아래 줄을 사용하고, 아니라면 주석 처리된 임시 코드를 사용해줘 ⭐
// import { useAuth } from '../context/AuthContext';


function RootLayout() {
  const navigate = useNavigate();

  // ⭐ AuthContext를 사용한다면 아래 2줄 대신 const { isAuthenticated, logout: authLogout } = useAuth();
  const [isAuthenticated, setIsAuthenticated] = useState(false); // 임시 로그인 상태

  useEffect(() => {
    // ⭐ 실제 로그인 여부는 로컬 스토리지에 토큰이 있는지 등으로 판단 (AuthContext 사용 시 더 체계적) ⭐
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token); // 토큰이 있으면 true, 없으면 false
  }, []);

  // ⭐⭐ 로그아웃 처리 함수 ⭐⭐
  const handleLogout = async () => {
    if (!window.confirm("로그아웃 하시겠습니까?")) {
      return;
    }
    try {
      // 백엔드 로그아웃 API 호출
      await api.post('/users/logout'); // 우리가 UserController에 만든 /api/users/logout 엔드포인트 호출
      
      // 프론트엔드에서 로그인 상태 초기화 (토큰 삭제 등)
      localStorage.removeItem('token'); // JWT 토큰 삭제
      localStorage.removeItem('refreshToken'); // 리프레시 토큰도 있다면 삭제
      setIsAuthenticated(false); // 로그인 상태 업데이트 (AuthContext 사용 시 authLogout() 호출)

      alert('로그아웃 되었습니다.');
      navigate('/login'); // 로그인 페이지로 이동
    } catch (error) {
      console.error('로그아웃 실패:', error);
      alert('로그아웃 실패: ' + (error.response?.data?.message || '알 수 없는 오류가 발생했습니다.'));
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* 헤더 섹션 */}
      <header className="bg-blue-600 text-white p-4 shadow-md flex justify-between items-center">
        <h1 className="text-xl font-bold">Spurfy</h1>
        <nav>
          {/* 로그인 상태에 따라 버튼 다르게 표시 */}
          {isAuthenticated ? ( // 로그인 되어 있을 때
            <div className="flex space-x-4">
              <button
                onClick={() => navigate('/mypage/profile')} // 마이페이지로 이동 버튼
                className="hover:underline"
              >
                마이페이지
              </button>
              <button
                onClick={handleLogout} // 로그아웃 버튼
                className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded"
              >
                로그아웃
              </button>
            </div>
          ) : ( // 로그인 되어 있지 않을 때
            <div className="flex space-x-4">
              <button
                onClick={() => navigate('/login')}
                className="hover:underline"
              >
                로그인
              </button>
              <button
                onClick={() => navigate('/signup')}
                className="hover:underline"
              >
                회원가입
              </button>
            </div>
          )}
        </nav>
      </header>

      {/* 메인 콘텐츠는 Outlet으로 표시 */}
      <main className="container mx-auto p-4">
        <Outlet /> {/* ⭐ 여기가 중요! 현재 라우트에 맞는 페이지 컴포넌트가 여기에 렌더링 ⭐ */}
      </main>
    </div>
  );
}

export default RootLayout;