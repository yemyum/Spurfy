import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import SpurfyButton from '../components/Common/SpurfyButton';
import WelcomeDog from '../assets/WelcomeDog.png';
import PremiumDog from '../assets/PremiumDog.png';
import RelaxingDog from '../assets/RelaxingDog.png';
import CalmingDog from '../assets/CalmingDog.png';

function SpaList() {
  const [list, setList] = useState([]);

  useEffect(() => {
    api.get('/spa-services')
      .then((res) => setList(res.data.data))
      .catch(() => alert('목록 불러오기 실패'));
  }, []);

  const spaImageMap = {
    "welcome-spa": WelcomeDog,
    "premium-brushing-spa": PremiumDog,
    "relaxing-therapy-spa": RelaxingDog,
    "calming-skin-spa": CalmingDog,
  };

  const spaBgColorMap = {
    'welcome-spa': 'bg-[#FFE4EC]',
    'premium-brushing-spa': 'bg-[#A4F0DF]',
    'relaxing-therapy-spa': 'bg-[#FFF9D8]',
    'calming-skin-spa': 'bg-[#E2E2FC]',
  };

  return (
    <div className="w-full mx-auto select-none bg-white rounded-xl shadow-md border border-gray-200 p-8">
      <h2 className="text-2xl font-semibold mb-8 text-spurfyBlue">스파 서비스</h2>

      <div className="flex flex-col space-y-8 mb-4">
        {list.map((spa) => (
          <div
            key={spa.serviceId}
            className="w-full bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col hover:ring-4 hover:ring-sky-100 transition-all duration-300 ease-in-out"
          >
            {/* 이미지 영역 */}
            <div
              className={`w-full h-[220px] sm:h-[380px] flex items-center justify-center rounded-xl mb-2
                ${spaBgColorMap[spa.slug] || 'bg-gray-100'}`}
            >
              {spaImageMap[spa.slug] ? (
                <img
                  src={spaImageMap[spa.slug]}
                  alt={`${spa.name} 썸네일`}
                  className="h-full object-contain"
                  style={{ maxWidth: "100%", maxHeight: "100%" }}
                />
              ) : (
                <span className="text-gray-400">이미지 준비중</span>
              )}
            </div>

            {/* 이름 + 가격 */}
            <div className="mb-2">
              <h3 className="text-lg font-semibold text-spurfyBlue">{spa.name}</h3>
              <p className="text-gray-800">가격: {spa.price.toLocaleString()} 원 ~</p>
            </div>

            {/* 태그 영역 */}
            <div className="flex flex-wrap gap-1 my-2">
              {spa.tagNames?.map((tagName, index) => (
                <span key={index} className="text-sm font-semibold bg-sky-50 text-spurfyLogo px-2 py-1 rounded-full">
                  #{tagName}
                </span>
              ))}
            </div>

            {/* 예약 버튼 */}
            <Link to={`/spalist/slug/${spa.slug}`}>
              <SpurfyButton variant="primary" className="w-full px-4 py-3 text-center mt-2">
                예약하러 가기
              </SpurfyButton>
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}

export default SpaList;