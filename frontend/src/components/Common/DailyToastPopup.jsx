import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faXmark } from '@fortawesome/free-solid-svg-icons';

const DailyToastPopup = () => {
  const [showToast, setShowToast] = useState(false);

  const STORAGE_KEY = 'dailyToastClosedTime'; // localStorage 키

  useEffect(() => {
    const closedTime = localStorage.getItem(STORAGE_KEY);
    const now = Date.now();
    const ONE_DAY = 24 * 60 * 60 * 1000; // 86,400,000 밀리초 = 24시간

    // 저장된 시간이 없거나 (첫 방문), 24시간이 지났으면 팝업을 띄움
    if (!closedTime || (now - closedTime > ONE_DAY)) {
      setShowToast(true);
    } else {
      // (2) 24시간이 안 지났으면 false를 유지 (없어도 됨, 다만 로직 명확화)
      setShowToast(false);
    }
  }, []);

  // 2. 닫기 버튼 핸들러
  const handleCloseToast = () => {
    // 현재 시각을 localStorage에 저장
    localStorage.setItem(STORAGE_KEY, String(Date.now()));
    // 상태를 업데이트해서 팝업을 닫음
    setShowToast(false);
  };

  if (!showToast) {
    return null; // showToast가 false면 아무것도 렌더링하지 않음
  }

  // 팝업 UI (fixed로 고정하여 채팅 영역과 독립적으로 움직임)
  return (
    <div className="fixed top-20 left-1/2 -translate-x-1/2 bg-teal-50 border-2 border-teal-200 rounded-xl shadow-md p-4 max-w-4xl z-40"
      style={{ width: 'calc(100% - 2rem)' }}>
      <div className="flex flex-col items-start text-sm">
        <p>
          스퍼피의 AI 어시스턴트, <span className="font-semibold text-base text-teal-400">"스피"</span>와 대화를 시작해보세요!
        </p>
        <p>
          <span className="font-semibold">반려견의 정면이 담긴 단독 사진</span>으로 올려주셔야 정확한 추천이 가능해요.<span className="text-gray-500 ml-1">(하루 최대 3회)</span>
        </p>

        {/* X 버튼 (닫기) */}
        <button
          onClick={handleCloseToast}
          className="absolute top-2 right-2 flex items-center justify-center"
          aria-label="토스트 팝업 닫기"
        >
          <FontAwesomeIcon icon={faXmark} className="w-4 h-4" />
        </button>
      </div>
    </div >
  );
};

export default DailyToastPopup;