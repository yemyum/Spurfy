import { NavLink, Outlet } from "react-router-dom";

function MypageLayout() {
  const menu = [
    { label: "내 프로필", path: "/mypage/profile" },
    { label: "반려견 케어", path: "/mypage/dogs" },
    { label: "예약 내역", path: "/mypage/reservations" },
    { label: "리뷰 조회", path: "/mypage/reviews" },
  ];

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* 모바일 탭 */}
      <nav className="grid grid-cols-4 gap-2 lg:hidden mb-4">
        {menu.map(m => (
          <NavLink
            key={m.path}
            to={m.path}
            className={({ isActive }) =>
              `text-center text-sm py-2 rounded-lg transition bg-white shadow-sm
               ${isActive ? "border border-gray-200 font-semibold" : "border border-gray-200 hover:bg-gray-50"}`
            }
          >
            {m.label}
          </NavLink>
        ))}
      </nav>

      <div className="lg:grid lg:grid-cols-[220px,1fr] gap-6">
        {/* 사이드바 */}
        <aside className="hidden lg:block">
          <div className="rounded-2xl border border-gray-200 bg-white shadow-sm p-4 py-6 sticky top-24">
            <h2 className="text-lg font-bold mb-4 px-2">마이페이지</h2>
            <ul className="space-y-2">
              {menu.map(m => (
                <li key={m.path}>
                  <NavLink
                    to={m.path}
                    className={({ isActive }) =>
                      `block w-full rounded-xl px-3 py-2 text-sm transition
                       ${isActive
                         ? "bg-blue-50 text-spurfyBlue font-semibold"
                         : "hover:bg-gray-50"}`
                    }
                  >
                    {m.label}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        </aside>

        {/* 본문 */}
        <main className="min-h-[560px]">
          <div className="rounded-2xl border border-gray-200 bg-white shadow-sm p-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default MypageLayout;