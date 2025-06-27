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
const handleLogout = () => {
  if (!window.confirm("로그아웃 하시겠습니까?")) {
    return;
  }
  // 1. 토큰 지우고,
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  // 2. 상태도 false로
  setIsAuthenticated(false);
  // 3. 안내 및 이동
  alert('로그아웃 되었습니다.');
  navigate('/login');
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