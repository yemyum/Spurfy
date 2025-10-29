import { NavLink, Outlet } from "react-router-dom";

function MypageLayout() {
  const menu = [
    { label: "내 프로필", path: "/mypage/profile" },
    { label: "반려견 케어", path: "/mypage/dogs" },
    { label: "예약 내역", path: "/mypage/reservations" },
    { label: "리뷰 내역", path: "/mypage/reviews" },
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
               ${isActive ? "bg-gradient-to-r from-[#54DAFF] to-[#91B2FF] font-semibold text-white shadow-md" : "border border-gray-200 shadow-md"}`
            }
          >
            {m.label}
          </NavLink>
        ))}
      </nav>

      <div className="lg:grid lg:grid-cols-[220px,1fr] gap-6">
        {/* 사이드바 */}
        <aside className="hidden lg:block sticky top-24">
          <div className="rounded-xl border border-gray-200 bg-white shadow-md p-4 py-4">
            <h2 className="text-lg font-semibold mb-4 px-2 border-b pb-2 border-b-gray-200">마이페이지</h2>
            <ul className="space-y-2">
              {menu.map(m => (
                <li key={m.path}>
                  <NavLink
                    to={m.path}
                    className={({ isActive }) =>
                      `block w-full rounded-lg px-3 py-2 transition
                       ${isActive
                         ? "bg-gradient-to-r from-[#54DAFF] to-[#91B2FF] text-white font-semibold shadow-md"
                         : "hover:bg-sky-100"}`
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
        <main className="min-h-[580px]">
          <div className="rounded-xl border border-gray-200 bg-white shadow-md p-2">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default MypageLayout;