import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import SpurfyDog from '../assets/SpurfyDog.png';
import SpurfyCA from '../assets/SpurfyCA.png';
import SpurfyButton from '../components/Common/SpurfyButton';

function Home() {
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // 컴포넌트 로드 시 로딩 시작
    document.body.classList.add("loading");

    setTimeout(() => {
      setLoading(false);
      document.body.classList.remove("loading");
    }, 100);
  }, []);

  // 추천받으러 가기 클릭 핸들러
  const handleGoAI = () => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인 후 이용 가능합니다.');
      navigate('/login');
      return;
    }
    navigate('/dog-spa-ai');
  };

  return (
    <div className="w-full flex flex-col items-center mt-10 mb-10">
      {/* 1. 상단 메인 배너 섹션 (왼쪽 텍스트 + 오른쪽 강아지 이미지) */}
      <div className="relative w-full overflow-hidden flex md:flex-row flex-col items-center min-h-[500px] md:h-[580px]">
        <div className="w-full md:w-1/3 h-full bg-white flex flex-col justify-center items-center md:items-start text-center md:text-left px-8 py-8 z-10 absolute md:static">
          <div className="md:ml-12 px-8">
            <h1 className="text-3xl md:text-3xl font-point text-stone-700 mb-10 leading-tight">
              우리 아이에게도 <br />
              하루쯤은 <span className="text-spurfyAI">스파데이</span>
            </h1>
            <p className="text-xl md:text-lg text-stone-400 font-semibold mb-20">
              매일 같은 일상 속, <br />
              오늘은 반려견에게 특별한 하루를 선물하세요.
            </p>
            <Link to="/spalist">
              <SpurfyButton
                variant="primary"
                className="px-4 py-2 text-2xl shadow-sm font-bold hover:shadow-md"
              >
                예약하러 가기
              </SpurfyButton>
            </Link>
          </div>
        </div>

        <div className="w-full md:w-2/3 h-[300px] md:h-full flex-shrink-0 overflow-hidden">
          <img src={SpurfyDog} alt="스파하는 귀여운 강아지" className="w-full h-full object-cover object-center" />
        </div>
      </div>

      {/* 2. AI 맞춤 분석 서비스 강조 섹션 */}
      {/* 2. AI 맞춤 분석 서비스 강조 섹션 */}
      <div className="w-full min-h-[500px] md:h-[580px]
                flex flex-col md:flex-row md:items-stretch
                md:p-28 bg-gradient-to-r from-white to-[#67F3EC]
                mt-16 mb-16 md:gap-10">

        {/* 왼쪽 텍스트 */}
        <div className="flex-1 md:basis-[420px] md:shrink-0
                  text-center md:text-left mt-6 px-6 md:px-0
                  break-keep whitespace-normal leading-snug">
          <h2 className="mt-6 md:mt-0 font-bold mb-8 md:mb-16
                   text-[clamp(20px,5.2vw,34px)]">
            스퍼피의 <span className="bg-gradient-to-r from-[#90F9BF] to-[#67F3EC] px-2 text-teal-700 rounded">AI 맞춤 분석</span> 서비스
          </h2>

          <div className="flex flex-col items-center md:items-start mb-8 md:mb-12 text-gray-800
                    space-y-3 sm:space-y-4 md:space-y-6">
            <ol className="
              list-decimal list-inside space-y-3 sm:space-y-4 md:space-y-6 break-keep leading-relaxed
              marker:font-bold marker:mr-2
              marker:text-lg sm:marker:text-xl md:marker:text-2xl
              text-[clamp(16px,4.2vw,20px)]
            ">
              <li>반려견 이미지를 AI가 분석해 <strong>견종과 특징을 파악</strong>합니다.</li>
              <li>체크리스트를 반영해 <strong>반려견에게 적합한 서비스를 선정</strong>합니다.</li>
              <li>분석 결과 바탕으로 <strong>최적의 서비스를 추천</strong>합니다.</li>
            </ol>
          </div>

          <button
            onClick={handleGoAI}
            className="px-4 py-2 text-lg sm:text-xl shadow-sm font-bold hover:shadow-md
                 bg-gradient-to-r from-[#90F9BF] to-[#67F3EC] rounded-lg text-white transition duration-300">
            추천받으러 가기
          </button>
        </div>

        {/* 오른쪽 이미지 */}
        <div className="flex justify-center md:justify-end items-center
                  flex-1 md:flex-1 md:min-w-0 px-6 md:px-0">
          <img
            src={SpurfyCA}
            alt="AI 분석 강아지"
            className="h-auto object-contain md:object-contain
                 max-w-[320px] sm:max-w-[380px] md:max-w-[580px]"
            loading="lazy"
          />
        </div>
      </div>
    </div>
  );
}

export default Home;
