import React, { useState } from 'react';

function HistoryItem({ data, onGoToSpaDetail }) {
  const [isExpanded, setIsExpanded] = useState(false); // 펼침 상태 관리
  const [imageLoadFailed, setImageLoadFailed] = useState(false);

  const toggleExpand = () => {
    setIsExpanded(!isExpanded);
  };

  // 'null' 문자열을 실제 null 값으로 처리 (백엔드에서 오는 데이터가 문자열 "null"일 경우 대비)
  const spaSlug = data.spaSlug === "null" ? null : data.spaSlug;
  const promptText = data.prompt || '질문 없음';
  // 추천 결과가 유효한지 판단하는 기준 (intro나 spaName이 있으면 유효하다고 볼게)
  const hasRecommendationResult = data.intro !== null || data.spaName !== null;

  return (
  <div className="mb-4 p-3 border border-purple-200 rounded-lg bg-white shadow-sm">
    {/* 핵심 정보 부분 - 클릭하면 펼쳐지게 */}
    <div onClick={toggleExpand} className="cursor-pointer flex items-center space-x-3 p-2 rounded-md hover:bg-gray-50 transition">
      {data.imageUrl && !imageLoadFailed ? ( // 1. imageUrl이 있고 AND 이미지 로드 실패하지 않았을 경우
        <img
          src={data.imageUrl}
          alt="강아지 사진"
          onError={() => setImageLoadFailed(true)}
          className="w-16 h-16 object-cover rounded-full flex-shrink-0 border-2 border-purple-300"
        />
      ) : ( // 2. imageUrl이 없거나 (null/빈 문자열), OR 이미지 로드에 실패했을 경우
        // "사진 없음" 텍스트 또는 플레이스홀더 이미지를 여기에 렌더링
        <div className="w-16 h-16 rounded-full flex-shrink-0 border-2 border-gray-300 bg-gray-100 flex items-center justify-center text-center text-gray-500 text-xs">
          사진<br/>없음
        </div>
        /*
        // 만약 플레이스홀더 이미지 사용할 경우 (상단 import 주석 해제 후)
        // import NoImagePlaceholder from '../assets/no_image_dog.png'; 가 선언되어 있어야 함!
        <img
          src={NoImagePlaceholder}
          alt="사진 없음"
          className="w-16 h-16 object-cover rounded-full flex-shrink-0 border-2 border-gray-300 bg-gray-100 p-2"
        />
        */
      )}
      <div className="flex-grow">
        <p className="text-gray-600 text-xs">{new Date(data.createdAt).toLocaleString()}</p>
        <p className="text-gray-800 text-sm font-semibold">
          질문: "{data.prompt || '질문 없음'}"
        </p>
        {data.spaName ? (
          <p className="text-purple-700 text-sm">
            추천 스파: <span dangerouslySetInnerHTML={{ __html: data.spaName.replace(/\*\*/g, '') }}></span>
          </p>
        ) : (
          <p className="text-gray-500 text-sm italic">추천 결과 없음</p>
        )}
      </div>
      {/* 펼침/닫힘 아이콘 추가 (예: ▼ / ▲) */}
      <span className="text-gray-500 text-lg">{isExpanded ? '▲' : '▼'}</span>
    </div>

    {/* GPT 상세 답변 부분 - isExpanded가 true일 때만 렌더링 */}
    {isExpanded && data.intro && (
      <div className="mt-3 pt-3 border-t border-gray-200">
        <p className="font-medium text-green-800 mb-1">{data.intro}</p>
        {data.compliment && <p className="text-green-700 mb-1">{data.compliment}</p>}
        {data.recommendationHeader && <p className="font-semibold text-green-800 mt-2 mb-1">{data.recommendationHeader}</p>}
        {data.spaDescription && data.spaDescription.length > 0 && (
          <ul className="list-disc list-inside text-green-700 text-sm mt-1">
            {data.spaDescription.map((desc, idx) => (
              <li key={idx}>{desc}</li>
            ))}
          </ul>
        )}
        {data.closing && <p className="text-green-800 mt-2">{data.closing}</p>}

        {data.spaSlug && data.spaSlug !== "null" && ( // null이 아닌 경우에만 버튼 표시
          <button
            onClick={() => onGoToSpaDetail(data.spaSlug)}
            className="mt-4 w-full py-2 px-4 bg-blue-500 text-white font-bold rounded-xl shadow hover:bg-blue-600 transition active:scale-95"
          >
            추천받은 스파로 예약하러 가기 →
          </button>
        )}
      </div>
    )}
    {isExpanded && !hasRecommendationResult && ( // 펼쳤는데 추천 결과가 없는 경우
        <div className="mt-3 pt-3 border-t border-gray-200 text-gray-500 text-sm italic text-center">
            이 기록에는 상세 추천 결과가 없습니다.
        </div>
    )}
  </div>
);
}

export default HistoryItem;