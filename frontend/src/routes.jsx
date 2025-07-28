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
import SpaReviewsPage from './pages/SpaReviewsPage';
import DogImageAnalysisPage from './pages/DogImageAnalysisPage';

const router = createBrowserRouter([
  {
    path: '/', // 웹사이트의 가장 기본 경로. 모든 페이지가 이 RootLayout 안에 들어갈 거
    element: <RootLayout />,
    children: [ 
      { path: '/', element: <Home /> },
      { path: '/about', element: <About /> },
      { path: '/dogs/register', element: <DogRegister /> },
      { path: '/dogs/:dogId/edit', element: <DogEdit /> },
      { path: '/spalist', element: <SpaList /> },
      { path: '/spalist/slug/:spaSlug', element: <SpaDetail /> },
      { path: '/spa-reviews/slug/:spaSlug', element: <SpaReviewsPage /> },
      { path: '/review/write', element: <ReviewWrite /> },
      { path: '/mypage/withdrawal', element: <WithdrawalPage /> },
      { path: '/dog-spa-ai', element: <DogImageAnalysisPage /> },
      {
        path: '/mypage',
        element: <MypageLayout />,
        children: [
          // 이 안의 path들은 부모의 경로(/mypage) 뒤에 붙임
          { path: 'profile', element: <Profile /> },
          { path: 'dogs', element: <MyDogs /> }, 
          { path: 'reservations', element: <MyReservationList /> },
          { path: 'reservations/:reservationId', element: <MyReservationDetail /> },
          { path: 'reviews', element: <MyReviewList /> },
          { path: 'reviews/:reviewId', element: <MyReviewDetail /> },
          // { path: 'mypage/withdrawal', element: <WithdrawalPage /> },
        ],
      },
    ],
  },
  { path: '/login', element: <Login /> },
  { path: '/signup', element: <Signup /> },
  { path: '/payment', element: <PaymentPage /> },
  { path: '/payment/:reservationId', element: <PaymentPage /> },
]);

export default router;