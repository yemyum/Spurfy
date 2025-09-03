// src/routes.jsx
import React from 'react';
import { createBrowserRouter } from 'react-router-dom';
import RootLayout from './layouts/RootLayout';

// 모든 페이지 컴포넌트들을 임포트 (네 기존 코드에서 가져옴)
import Home from './pages/Home';
import About from './pages/About';
import Signup from './pages/Signup';
import Login from './pages/Login';
import MypageLayout from './pages/MypageLayout';
import Profile from './pages/Profile';
import MyDogs from './pages/MyDogs';
import DogRegister from './pages/DogRegister';
import DogDetail from './pages/DogDetail';
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
import AIRecommendationPage from './pages/AIRecommendationPage';
import NotFoundPage from './pages/NotFoundPage';

const router = createBrowserRouter([
  {
    path: '/', // 웹사이트의 가장 기본 경로. 모든 페이지가 이 RootLayout 안에 들어갈 거
    element: <RootLayout />,
    errorElement: <NotFoundPage />,
    children: [ 
      { index: true, element: <Home /> },
      { path: 'about', element: <About /> },
      { path: 'spalist', element: <SpaList /> },
      { path: 'spalist/slug/:spaSlug', element: <SpaDetail /> },
      { path: 'mypage/withdrawal', element: <WithdrawalPage /> },
      {
        path: 'mypage',
        element: <MypageLayout />,
        errorElement: <NotFoundPage />,
        children: [
          { path: 'profile', element: <Profile /> },
          { path: 'dogs', element: <MyDogs /> },
          { path: 'dogs/register', element: <DogRegister /> },
          { path: 'dogs/:dogId', element: <DogDetail /> },
          { path: 'dogs/:dogId/edit', element: <DogEdit /> },
          { path: 'reservations', element: <MyReservationList /> },
          { path: 'reservations/:reservationId', element: <MyReservationDetail /> },
          { path: 'reviews', element: <MyReviewList /> },
          { path: 'reviews/:reviewId', element: <MyReviewDetail /> },
        ],
      },
    ],
  },
  { path: '/login', element: <Login /> },
  { path: '/signup', element: <Signup /> },
  { path: '/payment', element: <PaymentPage /> },
  { path: '/payment/:reservationId', element: <PaymentPage /> },
  { path: '/review/write', element: <ReviewWrite /> },
  { path: '/dog-spa-ai', element: <AIRecommendationPage /> },
  { path: '/spa-reviews/slug/:spaSlug', element: <SpaReviewsPage /> },

  { path: '*', element: <NotFoundPage /> }
]);

export default router;