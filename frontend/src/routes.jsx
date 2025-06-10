// src/routes.jsx
import React from 'react';
import Home from './pages/Home';
import About from './pages/About';
import Signup from './pages/Signup';
import Login from './pages/Login';
import Mypage from './pages/Mypage';
import DogRegister from './pages/DogRegister';
import MyDogs from './pages/MyDogs';
import DogEdit from './pages/DogEdit';

const routeList = [
  { path: '/', element: <Home /> },
  { path: '/about', element: <About /> },
  { path: '/signup', element: <Signup /> },
  { path: '/login', element: <Login /> },
  { path: '/mypage', element: <Mypage /> },
  { path: '/dogs/register', element: <DogRegister /> },
  { path: '/mypage/dogs', element: <MyDogs /> },
  { path: '/dogs/:dogId/edit', element: <DogEdit /> },

];

export default routeList;
