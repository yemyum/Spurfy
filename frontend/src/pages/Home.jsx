import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import SpurfyBanner from '../assets/SpurfyBanner.png';
import SpurfyButton from '../components/Common/SpurfyButton';

function Home() {
  const bannerRef = useRef(null);

  return (
  <div className="w-full flex flex-col items-center mt-10 mb-10">
    <div className="relative w-full h-[500px]" ref={bannerRef}>
    {/* 배경 이미지 */}
    <img
    src={SpurfyBanner}
    alt="스퍼피 메인 배너"
    className="absolute inset-0 w-full h-full object-cover"
    style={{
      objectPosition: "center top", // 필요하면 center center
    }}
    />
    </div>

    {/* 2. AI 맞춤 분석 서비스 강조 섹션 */}
      <div className="w-full bg-gradient-to-r from-white to-[#67F3EC] mt-16 mb-10">
        <div className='p-20'>
        <h2 className="text-4xl font-bold mb-10">
          스퍼피의 <span className="bg-gradient-to-r from-[#67F3EC] to-[#BFF7F3] px-2">AI 맞춤 분석</span> 서비스
        </h2>

        <div className="flex flex-col items-start mb-10 text-left space-y-6 text-lg text-gray-800">
          <div className="flex items-center">
            <span className="font-bold text-2xl mr-4">1.</span>
            <p className='text-xl'>
              반려견 사진을 AI가 분석해 <strong>견종과 특징</strong>을 파악합니다.
            </p>
          </div>
          <div className="flex items-center">
            <span className="font-bold text-2xl mr-4">2.</span>
            <p className='text-xl'>
              체크리스트를 반영해 <strong>반려견에게 알맞는 스파</strong>를 선정합니다.
            </p>
          </div>
          <div className="flex items-center">
            <span className="font-bold text-2xl mr-4">3.</span>
            <p className='text-xl'>
              분석 결과를 기반으로 <strong>최적의 스파 서비스</strong>를 추천합니다.
            </p>
          </div>
        </div>

        {/* AI 추천받으러 가기 버튼 (중앙 정렬) */}
        <Link to="/dog-spa-ai" className="inline-block">
          <SpurfyButton
            variant="ai"
            className="px-5 py-3 text-2xl shadow-sm"
          >
            추천받으러 가기
          </SpurfyButton>
        </Link>
        </div>
      </div>

    </div>
);
}

export default Home;
