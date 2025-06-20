// src/pages/MypageLayout.jsx
import React from 'react';
import { Outlet, Link } from 'react-router-dom';

function MypageLayout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <aside style={{ width: '200px', backgroundColor: '#f7f7f7', padding: '20px' }}>
        <h3>마이페이지</h3>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li><Link to="/mypage/profile">내 프로필</Link></li>
          <li><Link to="/mypage/dogs">반려견 케어</Link></li>
          <li><Link to="/mypage/reservations">예약 내역</Link></li>
          <li><Link to="/mypage/reviews">리뷰 조회</Link></li>
        </ul>
      </aside>

      <main style={{ flexGrow: 1, padding: '30px' }}>
        <Outlet />
      </main>
    </div>
  );
}

export default MypageLayout;