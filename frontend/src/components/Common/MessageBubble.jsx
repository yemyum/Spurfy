import React, { useMemo } from 'react';
import SpurfyButton from "./SpurfyButton";
import SpurfyAI from "../../assets/SpurfyAI.png";
import ReactMarkdown from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import gfm from 'remark-gfm';

function MessageBubble({ text, isUser, imageUrl, imageBase64, checklist, spaSlug, onGoToSpaDetail }) {
  const bubbleClasses = isUser
    ? "bg-[#00D8C2] text-white rounded-bl-xl rounded-tl-xl rounded-br-xl self-end"
    : "bg-gray-200 text-gray-800 rounded-br-xl rounded-bl-xl rounded-tr-xl self-start";

  // Markdown 파싱 결과 메모
  const renderedMarkdown = useMemo(() => {
    if (!text) return null;
    return (
      <ReactMarkdown
        remarkPlugins={[gfm, remarkBreaks]}
        components={{
          p: ({ node, ...props }) => <p className="mb-4" {...props} />,
          li: ({ node, ...props }) => <li className="list-disc list-inside ml-2 mb-1 last:mb-4" {...props} />
        }}
      >
        {text}
      </ReactMarkdown>
    );
  }, [text]);

  if (isUser) {
    return (
      <div className={`flex items-start flex-row-reverse gap-2`}>
        <div className={`max-w-[70%] p-4 flex flex-col ${bubbleClasses} relative`}>
          {/* 사진 영역 */}
          {(imageUrl || imageBase64) && (
            <div className="w-52 h-52 mb-2 overflow-hidden rounded-md">
              <img
                src={imageUrl || imageBase64}
                alt="사용자가 첨부한 사진"
                className="w-full h-full object-cover" />
            </div>
          )}

          {/* 체크리스트 영역 */}
          {checklist && Object.values(checklist).some(val =>
            (Array.isArray(val) && val.length > 0) || (typeof val === 'string' && val.trim() !== '')
          ) && (
              <div className="bg-white/30 text-white p-3 rounded-md mb-2 text-sm">
                {checklist.selectedBreed && checklist.selectedBreed !== '선택 안 함' && <p><strong>견종:</strong> {checklist.selectedBreed}</p>}
                {checklist.ageGroup && checklist.ageGroup !== '' && <p><strong>나이:</strong> {checklist.ageGroup}</p>}
                {checklist.activityLevel && checklist.activityLevel !== '' && <p><strong>활동량:</strong> {checklist.activityLevel}</p>}
                {checklist.healthIssues && checklist.healthIssues.length > 0 && <p><strong>건강 문제:</strong> {checklist.healthIssues.join(', ')}</p>}
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
        {spaSlug && onGoToSpaDetail && !isUser && (
          <SpurfyButton variant="primary" onClick={() => onGoToSpaDetail(spaSlug)} className="py-2 px-3 text-sm self-start">
            해당 스파로 예약하러 가기 →
          </SpurfyButton>
        )}
      </div>
    </div>
  );
}

export default React.memo(MessageBubble);