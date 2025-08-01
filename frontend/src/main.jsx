import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom'; // ⭐ BrowserRouter 대신 RouterProvider를 임포트! ⭐
import router from './routes'; // ⭐ routes.jsx에서 내보낸 'router' 객체를 임포트! ⭐
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    {/* ⭐⭐⭐ BrowserRouter 대신 RouterProvider를 사용하고, 만든 'router' 객체를 'router' prop으로 전달! ⭐⭐⭐ */}
    <RouterProvider router={router} />
  </React.StrictMode>,
);