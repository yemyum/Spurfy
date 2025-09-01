// auth.js (로그인/로그아웃 헬퍼, 순수 JS)
import api, { setAccessToken } from './axios';

// 로그인
export async function login(email, password) {
  const res = await api.post('/users/login', { email, password });
  const token = res?.data?.data ?? res?.data?.accessToken; // ApiResponse<String> or {accessToken}
  if (!token) throw new Error('로그인 응답에 accessToken 없음');
  setAccessToken(token);
  return res.data;
}

// 로그아웃
export async function logout() {
  try {
    await api.post('/users/logout');
  } catch (e) {
    console.error('logout error:', e);
  } finally {
    setAccessToken(null); // 토큰/헤더 초기화
    window.location.href = '/login'; // 로그인 페이지로 이동
  }
}