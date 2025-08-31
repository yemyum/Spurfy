// auth.js (로그인/로그아웃 헬퍼, 순수 JS)
import api, { setAccessToken } from './axios';

// 로그인
export async function login(email, password) {
  const res = await api.post('/users/login', { email, password });
  const token = res?.data?.accessToken; // 백엔드 응답 키 확인
  if (!token) throw new Error('로그인 응답에 accessToken 없음');
  setAccessToken(token);
  return res.data; // 필요하면 user 정보 포함
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