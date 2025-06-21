// src/routes.jsx
import React from 'react';
import Home from './pages/Home';
import About from './pages/About';
import Signup from './pages/Signup';
import Login from './pages/Login';
import MypageLayout from './pages/MypageLayout'; // 추가
import Profile from './pages/Profile'; // 새로 만들거나 마이페이지 컴포넌트로 연결
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

const routeList = [
  { path: '/', element: <Home /> },
  { path: '/about', element: <About /> },
  { path: '/signup', element: <Signup /> },
  { path: '/login', element: <Login /> },
  { path: '/dogs/register', element: <DogRegister /> },
  { path: '/dogs/:dogId/edit', element: <DogEdit /> },
  { path: '/spalist', element: <SpaList /> },
  { path: '/spalist/:id', element: <SpaDetail /> },
  { path: '/payment', element: <PaymentPage /> },
  { path: '/payment/:reservationId', element: <PaymentPage /> },
  {
    path: '/mypage',
    element: <MypageLayout />,
    children: [
      { path: 'profile', element: <Profile /> },
      { path: 'dogs', element: <MyDogs /> },
      { path: 'reservations', element: <MyReservationList /> },
      { path: 'reservations/:reservationId', element: <MyReservationDetail /> },
      { path: 'reviews', element: <MyReviewList /> },
      { path: 'reviews/:reviewId', element: <MyReviewDetail /> },
      { path: 'mypage/withdrawal', element: <WithdrawalPage /> },
    ],
  },
  { path: '/review/write', element: <ReviewWrite /> },
];

export default routeList;