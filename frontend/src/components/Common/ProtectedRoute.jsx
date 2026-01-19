import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');

  if (!token) {
    alert("로그인 후 이용 가능한 서비스입니다.");
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;