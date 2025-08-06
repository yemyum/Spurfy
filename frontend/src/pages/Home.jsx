import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import SpurfyDog from '../assets/SpurfyDog.png';
import SpurfyCA from '../assets/SpurfyCA.png';
import SpurfyButton from '../components/Common/SpurfyButton';

function Home() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 컴포넌트 로드 시 로딩 시작
    document.body.classList.add("loading");

    setTimeout(() => {
    setLoading(false);
    document.body.classList.remove("loading");
  }, 100); 
  }, []);

  return (
  <div className="w-full flex flex-col items-center mt-10 mb-10">
    {/* 1. 상단 메인 배너 섹션 (왼쪽 텍스트 + 오른쪽 강아지 이미지) */}  
        {/* 왼쪽: 메인 문구 영역 */}
        <div className="relative w-full overflow-hidden flex md:flex-row flex-col items-center min-h-[500px] md:h-[580px]">

        {/* 왼쪽: 텍스트 패널 영역 (항상 흰색 배경) */}
        {/* 작은 화면에서는 가로 꽉 채움, 큰 화면에서는 1/3 너비 */}
        <div className="w-full md:w-1/3 h-full bg-white flex flex-col justify-center items-center md:items-start text-center md:text-left px-8 py-8 z-10 absolute md:static">
          <div className="md:ml-12 px-8">
            <h1 className="text-3xl md:text-3xl font-logo text-stone-700 mb-10 leading-tight">
              우리 아이에게도 <br />
              하루쯤은 <span className="text-teal-300">스파데이</span>
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

        {/* 오른쪽: 강아지 이미지 영역 (나머지 공간을 모두 차지) */}
        {/* 작은 화면에서는 높이를 제한해서 텍스트 아래에 잘 보이도록, 큰 화면에서는 높이 꽉 채움 */}
        <div className="w-full md:w-2/3 h-[300px] md:h-full flex-shrink-0 overflow-hidden">
          <img
            src={SpurfyDog}
            alt="스파하는 귀여운 강아지"
            className="w-full h-full object-cover object-center"
          />
        </div>
      </div>

  {/* 2. AI 맞춤 분석 서비스 강조 섹션 */}
  <div className="w-full min-h-[500px] md:h-[580px] flex flex-col md:flex-row items-center md:p-28 bg-gradient-to-r from-white to-[#67F3EC] mt-16 mb-16 md:gap-10">
    
    {/* 왼쪽 텍스트 */}
    <div className="flex-1 text-center md:text-left mt-6">
      <h2 className="mt-6 md:mt-0 text-2xl md:text-4xl font-bold mb-8 md:mb-16">
        스퍼피의 <span className="bg-gradient-to-r from-[#90F9BF] to-[#67F3EC] px-2 text-teal-700">AI 맞춤 분석</span> 서비스
      </h2>

      <div className="flex flex-col items-center md:items-start mb-8 md:mb-12 text-gray-800 space-y-4 md:space-y-6">
        <div className="flex items-start">
          <span className="font-bold text-lg md:text-2xl mr-2 md:mr-4">1.</span>
          <p className='text-lg md:text-2xl'>
            반려견 사진을 AI가 분석해 <strong>견종과 특징</strong>을 파악합니다.
          </p>
        </div>
        <div className="flex items-start">
          <span className="font-bold text-lg md:text-2xl mr-2 md:mr-4">2.</span>
          <p className='text-lg md:text-2xl'>
            체크리스트를 반영해 <strong>반려견에게 알맞는 스파</strong>를 선정합니다.
          </p>
        </div>
        <div className="flex items-start">
          <span className="font-bold text-lg md:text-2xl mr-2 md:mr-4">3.</span>
          <p className='text-lg md:text-2xl'>
            분석 결과를 기반으로 <strong>최적의 스파 서비스</strong>를 추천합니다.
          </p>
        </div>
      </div>

      <Link to="/dog-spa-ai">
        <button className="px-4 py-2 text-2xl shadow-sm font-bold hover:shadow-md bg-gradient-to-r from-[#90F9BF] to-[#67F3EC] rounded-lg text-white hover:shadow-md">
          추천받으러 가기
        </button>
      </Link>
    </div>

    {/* 오른쪽 이미지 */}
    <div className="flex justify-center md:justify-end items-center flex-1">
      <img
        src={SpurfyCA}
        alt="AI 분석 강아지"
        className="h-auto md:h-full max-w-[350px] md:max-w-[580px] object-cover md:object-cover object-bottom self-end"
      />
    </div>

  </div>
</div>
);
}

export default Home;
