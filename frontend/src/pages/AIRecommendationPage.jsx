import React, { useState, useEffect, useRef, useCallback } from "react";
import api from "../api/axios";
import MessageBubble from "../components/Common/MessageBubble";
import ChecklistDrawer from "../components/Common/ChecklistDrawer";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCamera, faListCheck, faPaperPlane } from "@fortawesome/free-solid-svg-icons";
import { formatAiMessage, sanitizeText } from "../utils/formatAiMessage";

import { useChatHistory } from "../hooks/useChatHistory";
import { useBodyScrollLock } from "../hooks/useBodyScrollLock";
import { useChecklist } from "../hooks/useChecklist";
import { useAiCallLimit } from '../hooks/useAiCallLimit';

const AIRecommendationPage = () => {
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  const [selectedFile, setSelectedFile] = useState(null);
  const [freeTextQuestion, setFreeTextQuestion] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [showToast, setShowToast] = useState(false);

  const {
    isLimitExceeded,
    MAX_DAILY_CALLS,
    checkAndUpdateLimit
  } = useAiCallLimit(); // 훅 호출!

  // 대화/체크리스트 훅
  const { chatMessages, isLoading, addMessage, replaceMessage, removeMessage } = useChatHistory();

  const {
    sheetOpen, setSheetOpen,
    checklist, setChecklist,
    selectedCount,
    hasAnySelection,   // ← 불리언!
    toPayload,         // ← 정규화 함수
    handleChecklistSubmit, handleApplyChecklist,
  } = useChecklist();

  useBodyScrollLock(sheetOpen);

  useEffect(() => {
    if (!errorMessage) return;
    setShowToast(true);
    const t = setTimeout(() => setShowToast(false), 2400);
    return () => clearTimeout(t);
  }, [errorMessage]);

  // 로그인 체크
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      alert("로그인 후 이용 가능합니다.");
      navigate("/login");
    }
  }, [navigate]);

  // 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "auto", block: "end" });
  }, [chatMessages]);

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) {
      setSelectedFile(null);
      setErrorMessage("파일을 선택하지 않았습니다.");
      return;
    }
    setSelectedFile(file);
    setErrorMessage("");
  };

  const handleImageAnalysis = async (event) => {
    event.preventDefault();
    console.log("전송 버튼 눌림");
    setErrorMessage("");

    // 전송 버튼이 비활성화 상태면 바로 종료 (클릭 방지)
    if (isLimitExceeded) {
      alert(`하루 AI 대화 횟수(${MAX_DAILY_CALLS}회)를 초과했습니다. 내일 다시 시도해주세요!`);
      return;
    }

    const payloadChecklist = hasAnySelection ? toPayload() : null;

    if (!selectedFile) {
      setErrorMessage("사진은 필수입니다. 파일을 선택해주세요!");
      return;
    }

    // ✅ 1. 임시 유저 메시지의 고유 ID를 생성
    const userTempId = `temp-${Date.now()}`;
    const previewUrl = URL.createObjectURL(selectedFile);
    const userTs = Date.now();

    // ✅ 2. API 요청 전에 임시 유저 메시지를 추가
    addMessage({
      id: userTempId, // 이 임시 ID로 나중에 메시지를 찾아서 교체
      text: sanitizeText(freeTextQuestion),
      isUser: true,
      imageUrl: previewUrl,
      checklist: payloadChecklist,
      timestamp: userTs,
    });

    try {
      // 3. 업로드 + 분석 요청
      const formData = new FormData();
      formData.append("dogImageFile", selectedFile);
      formData.append("question", sanitizeText(freeTextQuestion));  // 빈 문자열도 허용 가능
      if (payloadChecklist) {
        formData.append("checklist", JSON.stringify(payloadChecklist));
      }

      const response = await api.post("/ai-recommendation", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const payload = response?.data?.data;
      if (!payload) {
        setErrorMessage("이미지 분석은 성공했지만 응답 형식이 이상합니다!");
        return;
      }

      // ✅ 4. 성공 응답을 받으면 임시 메시지를 진짜 메시지로 교체
      const serverId = payload.id;
      const userPromptId = `user-${serverId}`; // ✅ user- 접두사로 통일
      const aiResponseId = `ai-${serverId}`;   // ✅ ai- 접두사로 통일
      const serverImgUrl = payload.imageUrl;

      // (useChatHistory에서 만든 replaceMessage를 활용)
      replaceMessage(userTempId, userPromptId, serverImgUrl);

      // ✅ 5. AI 메시지 추가
      const aiResult = formatAiMessage(payload);
      addMessage({
        ...aiResult,
        isUser: false,
        imageUrl: null,
        id: aiResponseId, // 서버에서 받은 ID로 AI 메시지 ID 생성
      });

      await checkAndUpdateLimit();

    } catch (error) {
      let msg = "이미지 분석 요청 중 오류가 발생했습니다!";
      const apiMsg = error.response?.data?.message;
      const apiCode = error.response?.data?.code;

      if (apiMsg) msg = `오류: ${apiMsg}${apiCode ? ` (${apiCode})` : ""}`;
      else if (error.message) msg = `오류: ${error.message}`;

      const payloadId = error.response?.data?.data?.id;
      const serverImg = error.response?.data?.data?.imageUrl;

      if (apiCode === 'CONVERSATION_LIMIT_EXCEEDED') {
        // 횟수 초과 에러일 경우:
        msg = `하루 AI 대화 횟수(${MAX_DAILY_CALLS}회)를 초과했습니다. 내일 다시 시도해주세요!`;

        removeMessage(userTempId);

        alert(msg); // ⭐️⭐️⭐️ 중요: 여기서 함수 실행을 완전히 끝내서 아래 코드가 실행되지 않게 막기!

        return;

        // ✅ 6. 오류 응답을 받으면 메시지 처리
      } else if (payloadId) {
        // 서버 응답에 ID가 있을 때: 임시 유저 메시지를 서버 ID로 교체
        // ✅ 오류 응답 시 필요한 ID를 상수로 빼내기
        const userPromptId = `prompt-${payloadId}`;
        const aiResponseId = `ai-${payloadId}`;
        replaceMessage(userTempId, userPromptId, serverImg);

        // AI 오류 메시지 추가
        addMessage({
          id: aiResponseId,
          isUser: false,
          text: msg,
          errorMessage: msg,
          timestamp: Date.now(),
        });
      } else {
        // 서버 응답에 ID가 없을 때: 임시 유저 메시지 삭제
        removeMessage(userTempId);

        // AI 오류 메시지 추가
        addMessage({
          id: `ai-error-${Date.now()}`,
          isUser: false,
          text: msg,
          errorMessage: msg,
          timestamp: Date.now(),
        });
      }

    } finally {
      // 성공/실패와 상관없이 항상 실행
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      setSelectedFile(null);
      setFreeTextQuestion("");
    }
  };

  const handleGoToSpaDetail = useCallback(
    (spaSlug) => {
      if (spaSlug) navigate(`/spalist/slug/${spaSlug}`);
    },
    [navigate]
  );

  return (
    <div className="w-full h-screen mx-auto bg-white flex flex-col overflow-hidden">
      {/* 1. 고정될 헤더 영역 */}
      <div className="fixed top-0 left-0 right-0 z-50 bg-black/80 p-4 shadow-lg flex justify-center items-center relative">
        <h2 className="text-2xl font-bold text-spurfyAI">Spurfy AI Chat</h2>
        {/* 오른쪽 끝 이모지 버튼 */}
        <button
          onClick={() => setSheetOpen(true)}
          className="absolute right-4 top-1/2 -translate-y-1/2 px-2 p-2 text-spurfyAI flex items-center gap-2"
          title="체크리스트 열기"
        >
          <FontAwesomeIcon icon={faListCheck} />
          {selectedCount > 0 && (
            <span className="ml-1 text-xs w-4 h-4 font-semibold flex items-center justify-center rounded-full bg-[#67F3EC] text-black">
              {selectedCount}
            </span>
          )}
        </button>
      </div>

      {/* 2. 채팅 내용 영역 (flex-1로 남은 공간 전부 차지하고 스크롤!) */}
      <div className="flex-1 min-h-[120px] bg-gray-50 overflow-y-auto p-6 flex flex-col space-y-2">
        {isLoading ? (
          null
        ) : chatMessages.length > 0 ? (
          chatMessages.map((m) => (
            <MessageBubble
              key={m.id}
              text={m.text}
              isUser={m.isUser}
              imageUrl={m.imageUrl ?? m.image_url ?? null}
              spaSlug={m.spaSlug}
              onGoToSpaDetail={handleGoToSpaDetail}
            />
          ))
        ) : (
          <p className="text-center text-gray-500 p-20 empty-hint">
            스퍼피의 AI 어시스턴트, <span className="font-semibold">"스피"</span>와 대화를 시작해보세요!
          </p>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 3. 전송 영역 */}
      <div className="flex flex-col items-center flex-shrink-0 w-full bg-gray-100 p-2 px-4 pt-4">
        <form onSubmit={handleImageAnalysis} className="w-full flex items-center gap-4">
          <label htmlFor="dogImageFileInput" className="cursor-pointer">
            <input type="file" id="dogImageFileInput" accept="image/*" onChange={handleFileChange} className="hidden" />
            <span className="pl-2 bg-transparent text-gray-600">
              <FontAwesomeIcon icon={faCamera} size="lg" />
            </span>
          </label>
          {selectedFile && (
            <div className="relative w-14 h-14">
              <img
                src={URL.createObjectURL(selectedFile)}
                alt="미리보기"
                className="w-14 h-14 rounded-xl object-cover border border-gray-200 shadow-sm"
              />
              <button type="button" onClick={() => setSelectedFile(null)} className="absolute top-1 right-1 w-4 h-4 bg-white font-bold text-gray-400 rounded-full flex items-center justify-center text-[8px] hover:bg-gray-50 transition">
                ✕
              </button>
            </div>
          )}
          <textarea
            id="freeTextQuestion"
            rows="1"
            value={freeTextQuestion}
            onChange={(e) => setFreeTextQuestion(e.target.value)}
            className="flex-1 p-2 bg-white rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-200 transition duration-200 resize-none overflow-hidden"
            onInput={(e) => {
              e.target.style.height = 'auto';
              e.target.style.height = e.target.scrollHeight + 'px';
            }}
          ></textarea>
          <button
            type="submit"
            disabled={isLimitExceeded}
            className={`
              pr-2

              ${isLimitExceeded
                ? 'text-gray-400 cursor-not-allowed bg-transparent'
                : 'text-[#67F3EC] bg-transparent'}
                `}>
            <FontAwesomeIcon icon={faPaperPlane} size="lg" />
          </button>
        </form>
      </div>
      <p className="text-center bg-gray-100 pt-2 pb-1 text-[12px] leading-none text-gray-500 select-none pointer-events-none">
        스퍼피의 AI 어시스턴트 <span className="font-semibold">스피</span>에게 추천을 받아보세요!<br />
        스피는 아직 배우는 중이라 답변이 정확하지 않을 수 있어요.
      </p>

      <ChecklistDrawer
        sheetOpen={sheetOpen}
        onClose={() => setSheetOpen(false)}
        checklist={checklist}
        setChecklist={setChecklist}
        onChecklistSubmit={handleChecklistSubmit}
        onApply={handleApplyChecklist}
      // onReset은 지금 구조엔 안 씀 → 제거 or 내부 처리 방식 수정
      />

      {showToast && (
        <div className="fixed bottom-28 left-1/2 -translate-x-1/2 z-50"
          role="alert" aria-live="assertive">
          <div className="px-4 py-2 rounded-full bg-red-50 border-2 border-red-300 text-red-400 font-semibold text-sm shadow-sm">
            {errorMessage}
          </div>
        </div>
      )}
    </div >
  );
};

export default AIRecommendationPage;