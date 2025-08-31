import React, { useState, useEffect } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { logout } from '../api/auth';
import Home from '../pages/Home';

function RootLayout() {
  const navigate = useNavigate();

  const [isAuthenticated, setIsAuthenticated] = useState(false); // 임시 로그인 상태

  useEffect(() => {
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token); // 토큰이 있으면 true, 없으면 false
  }, []);


  const handleLogout = async () => {
    if (!window.confirm('로그아웃 하시겠습니까?')) return;
    await logout(); // auth.js에서 처리
  };

  return (
    <div className="bg-[#F1FAFF] min-h-screen select-none">
      <div className="min-h-screen flex flex-col">
        {/* 헤더 섹션 */}
        <header className="py-8 px-10 flex items-center justify-between w-full">
          {/* 1. 로고 */}
          <div className="flex-1 flex items-center">
            <Link to="/"
              className="cursor-pointer text-4xl font-logo font-bold bg-gradient-to-r from-[#54DAFF] to-[#91B2FF] bg-clip-text text-transparent"
            >
              SPURFY
            </Link>
          </div>

          {/* 2. 중앙 메뉴 */}
          <nav className="flex-1 flex justify-center">
            <div className="flex space-x-24 text-stone-700 text-sm font-semibold items-center">
              <Link to="/" className="hover:underline">Home</Link>
              <Link to="/spalist" className="hover:underline">Spa</Link>
              <Link to="/mypage/profile" className="hover:underline">Mypage</Link>
              <Link to="/about" className="hover:underline">About</Link>
            </div>
          </nav>

          {/* 3. 로그인/로그아웃 */}
          <div className="flex-1 flex justify-end items-center font-semibold text-lg">
            {isAuthenticated ? (
              <button
                onClick={handleLogout}
                className="px-5 py-2 font-logo bg-white border-2 border-gray-200 text-gray-700 rounded-lg shadow-sm transition hover:shadow-md hover:scale-105 transition duration-300"
              >
                Sign out
              </button>
            ) : (
              <button
                onClick={() => navigate('/login')}
                className="px-6 py-2 font-logo bg-gradient-to-r from-[#8EFAF5] via-[#54DAFF] to-[#91B2FF] rounded-lg text-white shadow-sm transition hover:shadow-md hover:scale-105 transition duration-300"
              >
                Sign in
              </button>
            )}
          </div>
        </header>


        {location.pathname === '/' && <Home />}

        {location.pathname !== '/' && (
          <main className="flex-1 max-w-[1200px] w-full mx-auto px-4">
            <Outlet />
          </main>
        )}
      </div>
    </div>
  );
}

export default RootLayout;