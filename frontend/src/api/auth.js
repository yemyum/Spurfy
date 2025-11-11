// auth.js (로그인/로그아웃 헬퍼, 순수 JS)
import api, { setAccessToken, beginForceLogout } from './axios';

// 로그인
export async function login(email, password) {
  const res = await api.post('/auth/login', { email, password }, { withCredentials: true });
  const token = res?.data?.data ?? res?.data?.accessToken; // ApiResponse<String> or {accessToken}
  if (!token) throw new Error('로그인 응답에 accessToken 없음');
  setAccessToken(token);
  // 다탭 동기화용 더미 핑
  try { localStorage.setItem('__auth_ping', String(Date.now())); } catch { }
  return res.data;
}

// 로그아웃
export async function logout() {
  try {
    beginForceLogout(); // 명시적 로그아웃 중엔 리프레시 금지
    await api.post('/auth/logout', {}, { withCredentials: true });
  } catch (e) {
    console.error('logout error:', e);
  } finally {
    setAccessToken(null); // 토큰/헤더 초기화 + localStorage 정리
    try { localStorage.setItem('__auth_ping', String(Date.now())); } catch {}
     // 화면 전환은 호출한 컴포넌트에서 navigate('/login')
  }
}