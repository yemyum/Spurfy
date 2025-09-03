import { NavLink, Outlet } from "react-router-dom";

function MypageLayout() {
  const menu = [
    { label: "내 프로필", path: "/mypage/profile" },
    { label: "반려견 케어", path: "/mypage/dogs" },
    { label: "예약 내역", path: "/mypage/reservations" },
    { label: "리뷰 조회", path: "/mypage/reviews" },
  ];

  return (
    <div className="max-w-6xl mx-auto">
      {/* 모바일 탭 */}
      <nav className="grid grid-cols-4 gap-2 lg:hidden mb-4">
        {menu.map(m => (
          <NavLink
            key={m.path}
            to={m.path}
            className={({ isActive }) =>
              `text-center p-2 rounded-lg transition bg-white
               ${isActive ? "bg-spurfyBlue/80 font-semibold text-white shadow-md" : "border border-gray-200 hover:bg-spurfyBlue/20 shadow-sm"}`
            }
          >
            {m.label}
          </NavLink>
        ))}
      </nav>

      <div className="lg:grid lg:grid-cols-[220px,1fr] gap-6">
        {/* 사이드바 */}
        <aside className="hidden lg:block">
          <div className="rounded-2xl border border-gray-200 bg-white shadow-sm p-4 py-5 sticky top-24">
            <h2 className="text-lg font-bold mb-4 px-2 border-b-2 pb-2 border-b-gray-200">마이페이지</h2>
            <ul className="space-y-2">
              {menu.map(m => (
                <li key={m.path}>
                  <NavLink
                    to={m.path}
                    className={({ isActive }) =>
                      `block w-full rounded-xl px-3 py-2 transition
                       ${isActive
                         ? "bg-spurfyBlue/80 text-white font-semibold shadow-md"
                         : "hover:bg-spurfyBlue/20"}`
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
          <div className="rounded-xl border border-gray-200 bg-white shadow-sm p-4">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default MypageLayout;