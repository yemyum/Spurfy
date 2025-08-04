import SpurfyButton from "./SpurfyButton";
import SpurfyAI from "../../assets/SpurfyAI.png";

function MessageBubble({ message, isUser, spaSlug, onGoToSpaDetail }) {
  const bubbleClasses = isUser
    ? "bg-[#00D8C2] text-white rounded-bl-xl rounded-tl-xl rounded-br-xl self-end" // 사용자 메시지 (오른쪽, 파란색)
    : "bg-gray-200 text-gray-800 rounded-br-xl rounded-bl-xl rounded-tr-xl self-start"; // AI 메시지 (왼쪽, 회색)

  return (
      <div className={`flex items-start ${isUser ? "flex-row-reverse" : "flex-row"} ${!isUser ? "gap-2" : ""}`}>
      {/* ✨ AI 프로필 이미지 (AI 메시지일 때만 표시) ✨ */}
      {!isUser && (
        <img
          src={SpurfyAI}
          alt="AI Profile"
          className="w-12 h-12 object-cover flex-shrink-0"
        />
      )}

      {/* 말풍선 본문 */}
      <div className={`max-w-[70%] p-4 mt-6 flex flex-col ${bubbleClasses} relative`}>
        <p className="whitespace-pre-wrap">{message}</p>
        {spaSlug && onGoToSpaDetail && !isUser && (
          <SpurfyButton variant="primary"
            onClick={() => onGoToSpaDetail(spaSlug)}
            className="mt-2 py-2 px-3 text-sm self-start"
          >
            해당 스파로 예약하러 가기 →
          </SpurfyButton>
        )}
      </div>
    </div>
  );
}

export default MessageBubble;