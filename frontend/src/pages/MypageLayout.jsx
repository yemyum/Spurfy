// src/pages/MypageLayout.jsx
import React from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';

function MypageLayout() {
  const location = useLocation();

  const menuItems = [
    { label: '내 프로필', path: '/mypage/profile' },
    { label: '반려견 케어', path: '/mypage/dogs' },
    { label: '예약 내역', path: '/mypage/reservations' },
    { label: '리뷰 조회', path: '/mypage/reviews' },
  ];

  return (
  <div className="w-full mx-auto mt-10 mb-10 bg-white rounded-xl shadow-md border border-gray-200 flex">
  {/* 사이드바 */}
  <aside className="w-56 border-r border-gray-200 py-6 p-3">
    <h2 className="text-xl font-bold px-6 py-2 mb-4">마이페이지</h2>
    <ul className="flex flex-col w-full">
      {menuItems.map((item, idx) => (
        <li key={item.path} className="w-full">
          <Link
            to={item.path}
            className={`w-full text-left px-6 py-3 block transition-all ${
              location.pathname === item.path
                ? 'font-semibold text-spurfyBlue'
                : 'text-gray-800 hover:text-spurfyBlue'
            }`}
          >
            {item.label}
          </Link>
          {idx !== menuItems.length - 1 && (
          <div className="border-b-2 border-gray-200 mx-2" />
        )}
        </li>
      ))}
    </ul>
  </aside>

      <main className="flex-1 p-8 min-h-[500px]">
        <Outlet />
      </main>
    </div>
  );
}

export default MypageLayout;