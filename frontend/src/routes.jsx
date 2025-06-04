// src/routes.jsx
import React from 'react';
import Home from './pages/Home';
import About from './pages/About';
// 필요한 페이지 컴포넌트들 import

const routeList = [
  { path: '/', element: <Home /> },
  { path: '/about', element: <About /> },
  // 추가 페이지도 자유롭게!
];

export default routeList;
