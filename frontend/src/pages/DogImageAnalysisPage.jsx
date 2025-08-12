import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import api from '../api/axios';
import ChecklistForm from "../components/Common/ChecklistForm";
import MessageBubble from "../components/Common/MessageBubble";
import ChecklistDrawer from "../components/Common/ChecklistDrawer";
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import { faListCheck } from '@fortawesome/free-solid-svg-icons';
import SpurfyButton from "../components/Common/SpurfyButton";

import { useChatHistory } from "../hooks/useChatHistory";
import { useBodyScrollLock } from "../hooks/useBodyScrollLock";
import { useChecklist } from "../hooks/useChecklist";

const DogImageAnalysisPage = () => {
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [freeTextQuestion, setFreeTextQuestion] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // ✅ 분리한 훅에서 상태와 함수를 가져와서 사용
  const { chatMessages, addMessage } = useChatHistory();
  const {
    sheetOpen,
    setSheetOpen,
    checklist,
    checklistDataRef,
    selectedCount,
    handleChecklistSubmit,
    handleApplyChecklist,
    handleResetChecklist
  } = useChecklist();

  // ✅ 훅을 함수처럼 호출해서 사용
  useBodyScrollLock(sheetOpen);

  // 진입 즉시 로그인 여부 확인
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인 후 이용 가능합니다.');
      navigate('/login');
    }
  }, [navigate]);

  // 스크롤 이동 로직 (남아있어야 함)
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'auto', block: 'end' });
  }, [chatMessages]);

  const sanitizeText = (t) => (t ? t.replace(/\\n/g, '\n').replace(/\r\n/g, '\n').trim() : '');

  const formatAiMessage = (result) => {
    const spaDescription = (result.spaDescription && result.spaDescription.length > 0)
      ? result.spaDescription.map(line => `- ${sanitizeText(line).replace(/^- /, '')}`).join('\n')
      : '';

    return {
      text: [
        sanitizeText(result.intro),
        sanitizeText(result.compliment),
        sanitizeText(result.recommendationHeader),
        sanitizeText(result.spaName),
        spaDescription,
        sanitizeText(result.closing)
      ].filter(Boolean).join('\n\n'),
      spaSlug: result.spaSlug,
      id: result.id,
      timestamp: new Date(result.createdAt).getTime(),
      imageUrl: result.imageUrl || null
    };
  };

  // handleFileChange는 여기서 직접 사용되므로 그대로 둠
  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) {
      setSelectedFile(null);
      setErrorMessage('파일을 선택하지 않았습니다.');
      return;
    }
    setSelectedFile(file);
    setErrorMessage('');
    e.target.value = "";
  };

  const handleImageAnalysis = async (event) => {
    event.preventDefault();
    checklistDataRef.current = { ... (checklistDataRef.current || {}), ...(checklist || {}) };


    if (!selectedFile) {
      setErrorMessage('사진은 필수입니다. 파일을 선택해주세요!');
      return;
    }

    const userMessageId = Date.now();
    const previewBase64 = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(selectedFile);
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
    });

    addMessage({
      id: userMessageId,
      text: sanitizeText(freeTextQuestion),
      isUser: true,
      imageBase64: previewBase64,
      checklist: checklistDataRef.current
    });

    try {
      const formData = new FormData();
      formData.append('dogImageFile', selectedFile);

      if (checklistDataRef.current) {
        formData.append('checklist', JSON.stringify(checklistDataRef.current));
      }
      if (freeTextQuestion) formData.append('question', freeTextQuestion);

      const response = await api.post('/dog-image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (response.data?.data) {
        const aiResult = formatAiMessage(response.data.data);

        addMessage({
          text: aiResult.text,
          isUser: false,
          spaSlug: aiResult.spaSlug,
          id: aiResult.id,
          timestamp: aiResult.timestamp,
          imageUrl: aiResult.imageUrl
        });
      } else {
        setErrorMessage('이미지 분석은 성공했지만 응답 형식이 이상합니다!');
      }

      setSelectedFile(null);
      checklistDataRef.current = null;
      setFreeTextQuestion('');
    } catch (error) {
      let msg = '이미지 분석 요청 중 오류가 발생했습니다!';
      if (error.response?.data?.message) {
        msg = `오류: ${error.response.data.message} (${error.response.data.code})`;
      } else if (error.message) {
        msg = `오류: ${error.message}`;
      }
      setErrorMessage(msg);
      addMessage({
        text: `AI 요청 실패: ${msg}`,
        isUser: false,
      });
    }
  };

  // SPA 상세 페이지 이동 함수는 그대로 둠
  const handleGoToSpaDetail = useCallback((spaSlug) => {
    if (spaSlug) navigate(`/spalist/slug/${spaSlug}`);
  }, [navigate]);

  return (
    <div className="w-full h-full mx-auto bg-white flex flex-col">
      <div className="fixed top-0 left-0 right-0 z-50 bg-black/80 p-4 shadow-lg flex justify-center items-center">
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

      <div className="flex-1 relative bg-gray-50 mt-[70px]">
        <div className="flex-1 bg-gray-50 overflow-y-auto p-6 pb-24 flex flex-col space-y-2">
          {chatMessages.length > 0 ? (
            chatMessages.map((msg) => (
              <MessageBubble
                key={msg.id}
                {...msg}
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
        </div>

        <div ref={messagesEndRef} />

        <form onSubmit={handleImageAnalysis} className="absolute bottom-0 left-0 right-0 z-40 w-full flex items-center gap-4 p-4 bg-gray-100">
          <label htmlFor="dogImageFileInput" className="cursor-pointer">
            <input
              type="file"
              id="dogImageFileInput"
              accept="image/*"
              onChange={handleFileChange}
              className="hidden"
            />
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
              <button
                type="button"
                onClick={() => setSelectedFile(null)}
                className="absolute top-1 right-1 w-4 h-4 bg-white font-bold text-black rounded-full flex items-center justify-center text-[8px] hover:bg-gray-50 transition"
              >
                ✕
              </button>
            </div>
          )}

          <textarea
            id="freeTextQuestion"
            rows="1"
            value={freeTextQuestion}
            onChange={(e) => setFreeTextQuestion(e.target.value)}
            placeholder="우리 반려견에 대해 궁금한 점이 있나요?"
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

      <ChecklistDrawer
        sheetOpen={sheetOpen}
        onClose={() => setSheetOpen(false)}
        checklist={checklist}
        onChecklistSubmit={handleChecklistSubmit}
        onApply={handleApplyChecklist}
        onReset={handleResetChecklist}
      />
    </div>
  );
};

export default DogImageAnalysisPage;