import SpurfyButton from "./SpurfyButton";
import SpurfyAI from "../../assets/SpurfyAI.png";
import ReactMarkdown from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import { marked } from 'marked';
import gfm from 'remark-gfm';

marked.setOptions({
  gfm: true,
  breaks: true
});

function MessageBubble({ text, isUser, imageUrl, checklist, spaSlug, onGoToSpaDetail }) {
  console.log('✅ MessageBubble로 들어온 message:', text);
  const bubbleClasses = isUser
    ? "bg-[#00D8C2] text-white rounded-bl-xl rounded-tl-xl rounded-br-xl self-end" // 사용자 메시지 (오른쪽, 파란색)
    : "bg-gray-200 text-gray-800 rounded-br-xl rounded-bl-xl rounded-tr-xl self-start"; // AI 메시지 (왼쪽, 회색)

  if (isUser) {
    return (
      <div className={`flex items-start flex-row-reverse gap-2`}>
        
        {/* 말풍선 본문 */}
        <div className={`max-w-[70%] p-4 mt-6 flex flex-col ${bubbleClasses} relative`}>
          {/* ⭐⭐ 1. 이미지를 먼저 띄우기 ⭐⭐ */}
          {imageUrl && (
            <div className="w-48 h-48 mb-4 overflow-hidden rounded-md">
              <img
                src={imageUrl}
                alt="사용자가 첨부한 사진"
                className="w-full h-full object-cover"
                onError={(e) => { // 이미지가 없을 때
                  e.target.style.display = 'none'; // 이미지를 숨김
                  e.target.nextElementSibling.style.display = 'block'; // 회색 네모 보여주기
                }}
              />
              <div 
                className="w-full h-full object-cover" 
                style={{ display: imageUrl ? 'none' : 'block' }} // 이미지가 없을 때만 보이도록
              ></div>
            </div>
          )}
          
          {/* ⭐⭐ 2. 체크리스트는 이쁘게 출력 (체크리스트 데이터가 있을 때만) ⭐⭐ */}
          {checklist && Object.values(checklist).some(val => (Array.isArray(val) && val.length > 0) || (typeof val === 'string' && val.trim() !== '')) && (
            <div className="bg-white bg-opacity-30 text-white p-3 rounded-md mb-2 text-sm">
              {checklist.selectedBreed && checklist.selectedBreed !== '선택 안 함' && <p><strong>견종:</strong> {checklist.selectedBreed}</p>}
              {checklist.ageGroup && checklist.ageGroup !== '' && <p><strong>나이:</strong> {checklist.ageGroup}</p>}
              {checklist.activityLevel && checklist.activityLevel !== '' && <p><strong>활동량:</strong> {checklist.activityLevel}</p>}
              {checklist.healthIssues && checklist.healthIssues.length > 0 && <p><strong>건강 문제:</strong> {checklist.healthIssues.join(', ')}</p>}
            </div>
          )}

          {/* ⭐⭐ 3. 질문 텍스트만 보여주기 ⭐⭐ */}
          {text && (
            <div className="whitespace-pre-wrap">
              {text}
            </div>
          )}
        </div>
      </div>
    );
  }

  // AI 메시지일 때는 기존 로직 그대로
  return (
    <div className={`flex items-start ${isUser ? "flex-row-reverse" : "flex-row"} ${!isUser ? "gap-2" : ""}`}>
      {!isUser && (
        <img
          src={SpurfyAI}
          alt="AI Profile"
          className="w-16 h-16 object-cover flex-shrink-0 rounded-full"
        />
      )}
      {/* 말풍선 본문 */}
   <div className={`max-w-[70%] p-6 mt-8 flex flex-col ${bubbleClasses} relative`}>
        {text && (
    <ReactMarkdown
      remarkPlugins={[gfm, remarkBreaks]}
      components={{
        p: ({node, ...props}) => <p className="mb-4" {...props} />,
        li: ({node, ...props}) => <li className="list-disc list-inside ml-2 mb-1 last:mb-4" {...props} />
      }}
    >
      {text}
    </ReactMarkdown>
  )}
  {spaSlug && onGoToSpaDetail && !isUser && (
    <SpurfyButton variant="primary"
      onClick={() => onGoToSpaDetail(spaSlug)}
      className="py-2 px-3 text-sm self-start"
    >
      해당 스파로 예약하러 가기 →
    </SpurfyButton>
  )}
</div>
    </div>
  );
}

export default MessageBubble;