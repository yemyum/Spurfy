import React, { useState, useEffect, useRef, useCallback } from "react";
import api from "../api/axios";
import MessageBubble from "../components/Common/MessageBubble";
import ChecklistDrawer from "../components/Common/ChecklistDrawer";
import SpurfyButton from "../components/Common/SpurfyButton";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCamera, faListCheck } from "@fortawesome/free-solid-svg-icons";

import { useChatHistory } from "../hooks/useChatHistory";
import { useBodyScrollLock } from "../hooks/useBodyScrollLock";
import { useChecklistStore } from "../hooks/useChecklistStore";

const DogImageAnalysisPage = () => {
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  const [selectedFile, setSelectedFile] = useState(null);
  const [freeTextQuestion, setFreeTextQuestion] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  // 대화/체크리스트 훅
  const { chatMessages, addMessage, replaceMessage } = useChatHistory(); // replaceMessage 추가
  const {
    sheetOpen,
    setSheetOpen,
    checklist,
    checklistDataRef,
    selectedCount,
    handleChecklistSubmit,
    handleApplyChecklist,
    handleResetChecklist,
  } = useChecklistStore();

  useBodyScrollLock(sheetOpen);

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

  const sanitizeText = (t) =>
    t ? t.replace(/\\n/g, "\n").replace(/\r\n/g, "\n").trim() : "";

  const formatAiMessage = (result) => {
    const spaDescription =
      result.spaDescription && result.spaDescription.length > 0
        ? result.spaDescription
          .map((line) => `- ${sanitizeText(line).replace(/^- /, "")}`)
          .join("\n")
        : "";

    return {
      // ✅ 에러 메시지가 있으면 그것만 text로 사용
      text: result.errorMessage
        ? sanitizeText(result.errorMessage)
        : [
          sanitizeText(result.intro),
          sanitizeText(result.compliment),
          sanitizeText(result.recommendationHeader),
          sanitizeText(result.spaName),
          spaDescription,
          sanitizeText(result.closing),
        ]
          .filter(Boolean)
          .join("\n\n"),
      spaSlug: result.spaSlug,
      id: result.id,
      timestamp: new Date(result.createdAt).getTime(),
      imageUrl: result.imageUrl || null,
      errorMessage: result.errorMessage || null, // ✅ errorMessage도 추가
    };
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) {
      setSelectedFile(null);
      setErrorMessage("파일을 선택하지 않았습니다.");
      return;
    }
    setSelectedFile(file);
    setErrorMessage("");
    e.target.value = "";
  };

  const handleImageAnalysis = async (event) => {
    event.preventDefault();
    setErrorMessage("");

    const hasAnySelection = (c) =>
      !!c &&
      ((c.selectedBreed && c.selectedBreed !== "선택 안 함") ||
        (Array.isArray(c.healthIssues) && c.healthIssues.length > 0) ||
        (typeof c.ageGroup === "string" && c.ageGroup.trim() !== "") ||
        (typeof c.activityLevel === "string" && c.activityLevel.trim() !== ""));

    checklistDataRef.current = hasAnySelection(checklist) ? checklist : null;

    if (!selectedFile) {
      setErrorMessage("사진은 필수입니다. 파일을 선택해주세요!");
      return;
    }

    const userMessageId = `temp-${Date.now()}`;
    const previewUrl = URL.createObjectURL(selectedFile);

    // ✅ API 요청 전에 임시 유저 버블을 추가!
    addMessage({
      id: `temp-user-${userMessageId}`,
      text: sanitizeText(freeTextQuestion),
      isUser: true,
      imageUrl: previewUrl,
      checklist: checklistDataRef.current,
    });

    try {
      // 2) 업로드 + 분석 요청
      const formData = new FormData();
      formData.append("dogImageFile", selectedFile);
      if (checklistDataRef.current) {
        formData.append("checklist", JSON.stringify(checklistDataRef.current));
      }
      if (freeTextQuestion) formData.append("question", freeTextQuestion);

      const response = await api.post("/dog-image", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const payload = response?.data?.data;
      if (!payload) {
        setErrorMessage("이미지 분석은 성공했지만 응답 형식이 이상합니다!");
        return;
      }

      // ✅ 2. 성공 응답을 받으면 임시 버블을 진짜 버블로 교체
      const permanentUserMsg = {
        id: `prompt-${payload.id}`,
        text: freeTextQuestion.trim(),
        isUser: true,
        imageUrl: payload.imageUrl,
        checklist: checklistDataRef.current,
        timestamp: new Date(payload.createdAt).getTime() - 1,
      };
      replaceMessage(userMessageId, permanentUserMsg);

      // 3. AI 버블 추가
      const aiResult = formatAiMessage(payload);
      addMessage({
        ...aiResult,
        isUser: false,
        imageUrl: null,
        id: `ai-${aiResult.id}`,
      });

      URL.revokeObjectURL(previewUrl);
      setSelectedFile(null);
      checklistDataRef.current = null;
      setFreeTextQuestion("");

    } catch (error) {
      URL.revokeObjectURL(previewUrl);

      let msg = "이미지 분석 요청 중 오류가 발생했습니다!";
      const apiMsg = error.response?.data?.message;
      const apiCode = error.response?.data?.code;

      // API 응답에서 에러 메시지를 가져와
      if (apiMsg) msg = `오류: ${apiMsg}${apiCode ? ` (${apiCode})` : ""}`;
      else if (error.message) msg = `오류: ${error.message}`;

      const payloadId = error.response?.data?.data?.id;
      const serverImg = error.response?.data?.data?.imageUrl;

      if (payloadId && serverImg) {
        const permanentUserMsg = {
          id: `prompt-${payloadId}`,
          text: freeTextQuestion.trim(),
          isUser: true,
          imageUrl: serverImg,
          checklist: checklistDataRef.current,
        };
        replaceMessage(userMessageId, permanentUserMsg);

        addMessage({
          id: `ai-${payloadId}`,
          isUser: false,
          text: msg,
          errorMessage: msg,
        });
      } else {
        setErrorMessage(msg);
      }

      setSelectedFile(null);
      checklistDataRef.current = null;
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
        <h2 className="text-2xl font-bold text-spurfyAI">SPURFY AI Chat</h2>
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
      <div className="flex-1 bg-gray-50 overflow-y-auto p-6 flex flex-col space-y-2">
        {chatMessages.length > 0 ? (
          chatMessages.map((m) => (
            <MessageBubble
              key={m.id}
              text={m.text}
              isUser={m.isUser}
              imageUrl={m.imageUrl ?? m.image_url ?? null} // snake_case 대비
              checklist={m.checklist}
              spaSlug={m.spaSlug}
              onGoToSpaDetail={handleGoToSpaDetail}
            />
          ))
        ) : (
          <p className="text-center text-gray-500 p-20">AI 챗봇과 대화를 시작해보세요!</p>
        )}
        {errorMessage && (
          <div className="py-2 px-4 rounded-lg text-center whitespace-pre-wrap font-semibold bg-red-50 text-red-600 text-sm">
            {errorMessage}
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 3. 전송 영역 */}
      <div className="flex flex-col items-center flex-shrink-0 w-full bg-gray-100 p-2 px-4 pt-4">
        <form onSubmit={handleImageAnalysis} className="w-full flex items-center gap-4">
          <label htmlFor="dogImageFileInput" className="cursor-pointer">
            <input type="file" id="dogImageFileInput" accept="image/*" onChange={handleFileChange} className="hidden" />
            <span className="p-2 rounded-full bg-[#67F3EC] text-black hover:bg-[#42e3db] transition">
              <FontAwesomeIcon icon={faCamera} />
            </span>
          </label>
          {selectedFile && (
            <div className="relative w-14 h-14">
              <img
                src={URL.createObjectURL(selectedFile)}
                alt="미리보기"
                className="w-14 h-14 rounded-xl object-cover border border-gray-200 shadow-sm"
              />
              <button type="button" onClick={() => setSelectedFile(null)} className="absolute top-1 right-1 w-4 h-4 bg-white font-bold text-black rounded-full flex items-center justify-center text-[8px] hover:bg-gray-50 transition">
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
          <SpurfyButton variant="chat" type="submit" className="py-2 px-4 text-sm font-semibold">
            전송
          </SpurfyButton>
        </form>
      </div>
      <p className="text-center bg-gray-100 pt-2 pb-1 text-[12px] leading-none text-gray-400 select-none pointer-events-none">
        스퍼피의 AI 어시스턴트 <span className="font-semibold">스피</span>에게 추천을 받아보세요!<br />
        스피는 아직 배우는 중이라서 답변이 정확하지 않을 수 있어요.
      </p>

      <ChecklistDrawer
        sheetOpen={sheetOpen}
        onClose={() => setSheetOpen(false)}
        checklist={checklist}
        onChecklistSubmit={handleChecklistSubmit}
        onApply={handleApplyChecklist}
        onReset={handleResetChecklist}
      />
    </div >
  );
};

export default DogImageAnalysisPage;