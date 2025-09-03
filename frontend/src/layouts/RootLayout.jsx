import React, { useState, useEffect, useLayoutEffect, useRef } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { logout } from '../api/auth';
import Home from '../pages/Home';

function RootLayout() {
  const headerRef = useRef(null);
  const [headerH, setHeaderH] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);
  const [isAuthed, setIsAuthed] = useState(!!localStorage.getItem('token'));
  const location = useLocation();
  const navigate = useNavigate();

  useLayoutEffect(() => {
    const el = headerRef.current;
    if (!el) return;

    const update = () => setHeaderH(el.offsetHeight);
    update(); // 초기 측정

    const ro = new ResizeObserver(update);
    ro.observe(el);

    // 혹시 모를 회전/리사이즈 대비
    window.addEventListener('resize', update, { passive: true });

    return () => {
      ro.disconnect();
      window.removeEventListener('resize', update);
    };
  }, [menuOpen]);

  // storage 이벤트로 다탭 동기화
  useEffect(() => {
    const onStorage = (e) => {
      if (e.key === 'token' || e.key === '__auth_ping') {
        setIsAuthed(!!localStorage.getItem('token'));
      }
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  // 라우트 변경 시 모바일 메뉴 닫기
  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const onLogout = async () => {
    if (!window.confirm('로그아웃 하시겠습니까?')) return;
    await logout();            // 토큰/헤더 정리
    setIsAuthed(false);        // 로컬 상태 갱신
    navigate('/login');        // ✅ SPA 네비 (전체 리로드 X)
  };

  return (
    <div className="bg-[#F1FAFF] min-h-svh select-none overflow-x-hidden">
      <div className="min-h-svh flex flex-col">
        {/* 헤더 */}
        <header
          ref={headerRef}
          className="fixed inset-x-0 top-0 z-50 bg-[#F1FAFF]/80 backdrop-blur-md">
          <div className="mx-auto w-full max-w-screen-xl px-4 sm:px-6 lg:px-10 py-4 sm:py-6 flex items-center justify-between gap-3">
            {/* 1) 로고 */}
            <div className="flex-1 min-w-0">
              <Link
                to="/"
                className="inline-block cursor-pointer truncate text-2xl sm:text-3xl lg:text-4xl font-logo font-bold bg-gradient-to-r from-[#54DAFF] to-[#91B2FF] bg-clip-text text-transparent"
              >
                SPURFY
              </Link>
            </div>

            {/* 2) 중앙 메뉴 (md 이상) */}
            <nav className="hidden md:flex flex-1 justify-center">
              <div className="flex items-center text-stone-700 text-sm font-semibold gap-6 lg:gap-12 xl:gap-24">
                <Link to="/" className="hover:underline">Home</Link>
                <Link to="/spalist" className="hover:underline">Spa</Link>
                <Link to="/mypage/profile" className="hover:underline">Mypage</Link>
                <Link to="/about" className="hover:underline">About</Link>
              </div>
            </nav>

            {/* 3) 우측 버튼 + 햄버거 */}
            <div className="flex-1 min-w-0 flex items-center justify-end gap-4">
              {isAuthed ? (
                <button
                  onClick={onLogout}
                  className="px-3 py-1.5 sm:px-5 sm:py-2 font-logo font-semibold text-gray-700 text-lg bg-white border-2 border-gray-200 rounded-lg shadow-sm transition hover:shadow-md hover:scale-[1.02]"
                >
                  Sign out
                </button>
              ) : (
                <button
                  onClick={() => navigate('/login')}
                  className="px-3 py-1.5 sm:px-6 sm:py-2 font-logo font-semibold text-white text-lg bg-gradient-to-r from-[#8EFAF5] via-[#54DAFF] to-[#91B2FF] rounded-lg shadow-sm transition hover:shadow-md hover:scale-[1.02]"
                >
                  Sign in
                </button>
              )}

              {/* 햄버거: md 미만 표시 */}
              <button
                onClick={() => setMenuOpen((v) => !v)}
                aria-label="Open menu"
                className="md:hidden inline-flex items-center justify-center text-gray-500"
              >
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                  <path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" strokeWidth="1.5" />
                </svg>
              </button>
            </div>
          </div>

          {/* 모바일 드롭다운 메뉴 */}
          {menuOpen && (
            <div className="md:hidden bg-white/80 backdrop-blur-md border-b border-gray-100 shadow-sm">
              <nav className="mx-auto max-w-screen-xl px-4 py-3">
                <ul className="flex flex-col gap-6 mt-4 mb-4 text-stone-700 text-base font-semibold">
                  <li><Link to="/" className="block hover:underline">Home</Link></li>
                  <li><Link to="/spalist" className="block hover:underline">Spa</Link></li>
                  <li><Link to="/mypage/profile" className="block hover:underline">Mypage</Link></li>
                  <li><Link to="/about" className="block hover:underline">About</Link></li>
                </ul>
              </nav>
            </div>
          )}
        </header>

        {/* ✅ 헤더 전체 높이만큼 자리 확보 — 본문을 아래로 밀어줌 */}
        <div style={{ height: headerH }} />

        {/* ✅ 공통 여백을 여기서만 관리 */}
        <div className="mt-4 sm:mt-6 mb-6">
          {location.pathname === '/' ? (
            // 홈: 풀블리드 그대로
            <Home />
          ) : (
            // 나머지: 컨테이너
            <main className="mx-auto w-full max-w-screen-xl px-4 sm:px-6 lg:px-8">
              <Outlet />
            </main>
          )}
        </div>
      </div>
    </div>
  );
}

export default RootLayout;