// src/routes.jsx
import React from 'react';
import { createBrowserRouter } from 'react-router-dom'; // ⭐ createBrowserRouter 임포트 확인 ⭐
import RootLayout from './layouts/RootLayout'; // ⭐ RootLayout 임포트 확인 ⭐

// 모든 페이지 컴포넌트들을 임포트 (네 기존 코드에서 가져옴)
import Home from './pages/Home';
import About from './pages/About';
import Signup from './pages/Signup';
import Login from './pages/Login';
import MypageLayout from './pages/MypageLayout';
import Profile from './pages/Profile';
import MyDogs from './pages/MyDogs';
import DogRegister from './pages/DogRegister';
import DogEdit from './pages/DogEdit';
import MyReservationList from './pages/MyReservationList';
import MyReviewList from './pages/MyReviewList';
import MyReviewDetail from './pages/MyReviewDetail';
import ReviewWrite from './pages/ReviewWrite';
import SpaList from './pages/SpaList';
import SpaDetail from './pages/SpaDetail';
import PaymentPage from './pages/PaymentPage';
import MyReservationDetail from './pages/MyReservationDetail';
import WithdrawalPage from './pages/Withdrawal';

// ⭐⭐⭐ 여기가 가장 중요! createBrowserRouter를 사용해서 라우터를 정의 ⭐⭐⭐
const router = createBrowserRouter([
  {
    path: '/', // 웹사이트의 가장 기본 경로. 모든 페이지가 이 RootLayout 안에 들어갈 거
    element: <RootLayout />, // ⭐ 최상위 레이아웃으로 RootLayout 설정 ⭐
    children: [ // ⭐ 여기에 RootLayout의 영향을 받는 모든 페이지 라우트들을 넣어줌 ⭐
      { path: '/', element: <Home /> }, // 메인 홈 페이지 (path가 '/'인 최상위 라우트 안에 또 '/'가 있으면, index: true와 같은 의미)
      { path: '/about', element: <About /> },
      { path: '/signup', element: <Signup /> },
      { path: '/login', element: <Login /> },
      { path: '/dogs/register', element: <DogRegister /> },
      { path: '/dogs/:dogId/edit', element: <DogEdit /> },
      { path: '/spalist', element: <SpaList /> },
      { path: '/spalist/:id', element: <SpaDetail /> },
      { path: '/payment', element: <PaymentPage /> },
      { path: '/payment/:reservationId', element: <PaymentPage /> },
      { path: '/review/write', element: <ReviewWrite /> },
      // ⭐ 회원 탈퇴 페이지는 MypageLayout 안이 아닌, RootLayout 바로 아래에 두는 것이 일반적 ⭐
      // 그래야 MypageLayout의 사이드바 같은 요소 없이, 메인 헤더만 있는 상태로 탈퇴 페이지를 볼 수 있음!
      { path: '/mypage/withdrawal', element: <WithdrawalPage /> },

      // ⭐ 마이페이지 관련 라우트들은 MypageLayout을 부모로 사용 (이전과 동일) ⭐
      {
        path: '/mypage', // /mypage 경로에 접근하면 MypageLayout이 렌더링
        element: <MypageLayout />,
        children: [
          // 이 안의 path들은 부모의 경로(/mypage) 뒤에 붙임
          { path: 'profile', element: <Profile /> }, // 최종 경로: /mypage/profile
          { path: 'dogs', element: <MyDogs /> },     // 최종 경로: /mypage/dogs
          { path: 'reservations', element: <MyReservationList /> }, // 최종 경로: /mypage/reservations
          { path: 'reservations/:reservationId', element: <MyReservationDetail /> }, // 최종 경로: /mypage/reservations/:reservationId
          { path: 'reviews', element: <MyReviewList /> }, // 최종 경로: /mypage/reviews
          { path: 'reviews/:reviewId', element: <MyReviewDetail /> }, // 최종 경로: /mypage/reviews/:reviewId
          // { path: 'mypage/withdrawal', element: <WithdrawalPage /> },
        ],
      },
    ],
  },
]);

export default router;