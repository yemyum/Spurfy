import React, { useMemo } from 'react';
import SpurfyButton from "./SpurfyButton";
import SpurfyAI from "../../assets/SpurfyAI.png";
import ReactMarkdown from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import gfm from 'remark-gfm';
import { faAngleRight } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

function MessageBubble({ text, isUser, imageUrl, checklist, spaSlug, onGoToSpaDetail, errorMessage }) {
  const bubbleClasses = isUser
    ? "bg-[#00D8C2] text-white rounded-bl-xl rounded-tl-xl rounded-br-xl self-end"
    : "bg-gray-200 text-gray-800 rounded-br-xl rounded-bl-xl rounded-tr-xl self-start";

  const toAbs = (u) => {
    if (!u) return null;
    if (/^(https?:|data:)/i.test(u)) return u; // 절대/data
    if (String(u).startsWith('blob:')) return u; // 미리보기 허용
    const base = (import.meta.env.VITE_IMAGE_BASE_URL || '').replace(/\/$/, '');
    return `${base}${u.startsWith('/') ? '' : '/'}${u}`;
  };

  const renderedMarkdown = useMemo(() => {
    if (!text) return null;
    return (
      <ReactMarkdown
        remarkPlugins={[gfm, remarkBreaks]}
        components={{
          p: (props) => <p className="mb-4" {...props} />,
          li: (props) => <li className="list-disc list-inside ml-2 mb-1 last:mb-4" {...props} />
        }}
      >
        {text}
      </ReactMarkdown>
    );
  }, [text]);

  const safeChecklist = checklist || {};
  const hasAnySelection = (c) => !!c && (
    (c.selectedBreed && c.selectedBreed !== '선택 안 함') ||
    (typeof c.ageGroup === 'string' && c.ageGroup.trim() !== '') ||
    (typeof c.activityLevel === 'string' && c.activityLevel.trim() !== '') ||
    (Array.isArray(c.healthIssues) && c.healthIssues.length > 0)
  );

  const resolvedSrc = useMemo(() => toAbs(imageUrl), [imageUrl]);

  if (isUser) {
    // 디버그: 이미지가 안 보이면 이 로그로 src 확인
    if (imageUrl) console.log('MessageBubble user image:', { imageUrl, resolvedSrc });

    return (
      <div className="flex items-start flex-row-reverse gap-2">
        <div className={`max-w-[70%] p-4 flex flex-col ${bubbleClasses} relative`}>

          {/* 사진 영역 */}
          {resolvedSrc && (
            <div className="w-52 h-52 mb-2 overflow-hidden rounded-md">
              <img
                src={resolvedSrc}
                alt="사용자가 첨부한 사진"
                className="w-full h-full object-cover"
                loading="lazy"
                onError={(e) => { e.currentTarget.style.display = 'none'; }}
              />
            </div>
          )}

          {/* 체크리스트 영역 */}
          {hasAnySelection(safeChecklist) && (
            <div className="bg-white/30 text-white p-3 rounded-md mb-2 text-sm">
              {safeChecklist.selectedBreed && safeChecklist.selectedBreed !== '선택 안 함' && (
                <p><strong>견종:</strong> {safeChecklist.selectedBreed}</p>
              )}
              {safeChecklist.ageGroup && safeChecklist.ageGroup.trim() !== '' && (
                <p><strong>나이대:</strong> {safeChecklist.ageGroup}</p>
              )}
              {safeChecklist.activityLevel && safeChecklist.activityLevel.trim() !== '' && (
                <p><strong>활동량:</strong> {safeChecklist.activityLevel}</p>
              )}
              {Array.isArray(safeChecklist.healthIssues) && safeChecklist.healthIssues.length > 0 && (
                <p><strong>건강 문제:</strong> {safeChecklist.healthIssues.join(', ')}</p>
              )}
            </div>
          )}

          {/* 텍스트 영역 */}
          {text && <div className="whitespace-pre-wrap">{text}</div>}
        </div>
      </div>
    );
  }

  return (
    <div className={`flex items-start ${isUser ? "flex-row-reverse" : "flex-row"} ${!isUser ? "gap-2" : ""}`}>
      {!isUser && (
        <img src={SpurfyAI} alt="AI Profile" className="w-16 h-16 object-cover flex-shrink-0 rounded-full" />
      )}
      <div className={`max-w-[70%] p-4 mt-8 flex flex-col ${bubbleClasses} relative`}>
        {renderedMarkdown}
        {spaSlug && onGoToSpaDetail && !errorMessage && (
          <SpurfyButton variant="primary" onClick={() => onGoToSpaDetail(spaSlug)} className="py-2 px-3 text-sm self-start">
            해당 스파로 예약하러 가기 <FontAwesomeIcon icon={faAngleRight} className="text-xs" />
          </SpurfyButton>
        )}
      </div>
    </div>
  );
}

export default React.memo(MessageBubble);