import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import SpurfyDog from '../assets/SpurfyDog.png';
import SpurfyButton from '../components/Common/SpurfyButton';

function Home() {

  return (
  <div className="w-full flex flex-col items-center mt-10 mb-10">
    {/* 1. 상단 메인 배너 섹션 (왼쪽 텍스트 + 오른쪽 강아지 이미지) */}  
          {/* 왼쪽: 메인 문구 영역 */}
        <div className="relative w-full overflow-hidden flex md:flex-row flex-col items-center min-h-[500px] md:h-[580px]"> {/* md:h-[550px]로 큰 화면 높이 고정 */}

        {/* 왼쪽: 텍스트 패널 영역 (항상 흰색 배경) */}
        {/* 작은 화면에서는 가로 꽉 채움, 큰 화면에서는 1/3 너비 */}
        <div className="w-full md:w-1/3 h-full flex flex-col justify-center items-center md:items-start text-center md:text-left bg-white px-8 py-8">
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
                className="px-4 py-2 text-2xl shadow-sm font-bold"
              >
                예약하러 가기
              </SpurfyButton>
            </Link>
          </div>
        </div>

        {/* 오른쪽: 강아지 이미지 영역 (나머지 공간을 모두 차지) */}
        {/* 작은 화면에서는 높이를 제한해서 텍스트 아래에 잘 보이도록, 큰 화면에서는 높이 꽉 채움 */}
        <div className="w-full md:w-2/3 h-[300px] md:h-full flex-shrink-0 mt-4 md:mt-0 overflow-hidden">
          <img
            src={SpurfyDog}
            alt="스파하는 귀여운 강아지"
            className="w-full h-full object-cover object-center"
          />
        </div>
      </div>


    {/* 2. AI 맞춤 분석 서비스 강조 섹션 */}
      <div className="w-full min-h-[580px] p-28 bg-gradient-to-r from-white to-[#67F3EC] mt-16 mb-16">
        <h2 className="text-4xl font-bold mb-16">
          스퍼피의 <span className="bg-gradient-to-r from-[#67F3EC] to-[#BFF7F3] px-2 text-teal-500">AI 맞춤 분석</span> 서비스
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
);
}

export default Home;
