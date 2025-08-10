import { useState, useEffect, useRef, useCallback } from "react";
import api from '../api/axios';
import ChecklistForm from "../components/Common/ChecklistForm";
import MessageBubble from "../components/Common/MessageBubble";
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import SpurfyButton from "../components/Common/SpurfyButton";

const DogImageAnalysisPage = () => {
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  const [chatMessages, setChatMessages] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const checklistDataRef = useRef(null);
  const [freeTextQuestion, setFreeTextQuestion] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isInitialLoad, setIsInitialLoad] = useState(true);

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
      imageUrl: result.imageUrl || null // ✅ 서버에서 온 URL
    };
  };

  useEffect(() => {
    const loadAndMergeMessages = async () => {
      let serverAiMessages = [];
      try {
        const response = await api.get('/recommendations/history');
        if (response.data?.data) {
          serverAiMessages = response.data.data.map(item => ({
            ...formatAiMessage(item),
            isUser: false
          }));
        }
      } catch (error) {
        console.error('❌ AI 기록 불러오기 실패:', error);
      }

      const savedLocalMessages = JSON.parse(localStorage.getItem("chatMessages")) || [];
      const localMessages = savedLocalMessages.map(msg => {
        if (!msg.text && msg.message) {
          msg.text = msg.message;
          delete msg.message;
        }
        // ✅ 혼합 처리: imageUrl 없고 base64 있으면 그대로 유지
        return {
          ...msg,
          imageUrl: msg.imageUrl || null,
          imageBase64: msg.imageBase64 || null
        };
      });

      const uniqueLocalMessages = localMessages.filter(m => m.isUser);
      const combinedMessages = [...uniqueLocalMessages, ...serverAiMessages];

      const finalMap = new Map();
      combinedMessages.forEach(msg => finalMap.set(msg.id, msg));

      const sorted = Array.from(finalMap.values()).sort((a, b) => a.timestamp - b.timestamp);
      setChatMessages(sorted);
    };

    loadAndMergeMessages();
  }, []);

  useEffect(() => {
  messagesEndRef.current?.scrollIntoView({ behavior: 'auto', block: 'end' });
}, [chatMessages]);

  // 메시지 추가 시 저장 구조
  const addMessage = (newMessageObj) => {
    setChatMessages((prev) => {
        const msg = {
          id: newMessageObj.id || Date.now(),
          timestamp: newMessageObj.timestamp || Date.now(),
          ...newMessageObj,
        };
        const msgForStorage = { ...msg };
        localStorage.setItem("chatMessages", JSON.stringify([...prev, msgForStorage]));
        return [...prev, msg];
    });
  };

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

  const handleChecklistSubmit = (data) => {
  console.log("체크리스트 데이터 변경:", data);
  checklistDataRef.current = data;
};

  const handleImageAnalysis = async (event) => {
    event.preventDefault();

    if (!selectedFile) {
      setErrorMessage('사진은 필수입니다. 파일을 선택해주세요!');
      return;
    }

    // ✅ 유저 메시지 추가 (이미지 URL은 서버 응답 후)
    const userMessageId = Date.now();
    // ✅ base64 미리보기: 신규는 imageUrl 없이 표시
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
      imageBase64: previewBase64, // 임시 표시용
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

        // ✅ AI 메시지에 서버 URL 포함
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
        text: `❌ AI 요청 실패: ${msg}`,
        isUser: false,
      });
    }
  };

  const handleGoToSpaDetail = useCallback((spaSlug) => {
    if (spaSlug) navigate(`/spalist/slug/${spaSlug}`);
  }, [navigate]);

  return (
    <div className="w-full h-full mx-auto bg-white mt-10 mb-10 overflow-hidden">
      <div className="fixed top-0 left-0 right-0 z-50 bg-black/80 p-4 shadow-lg flex justify-center items-center">
        <h2 className="text-2xl font-bold text-spurfyAI">Spurfy AI Chat</h2>
      </div>

      <div className="flex-1 relative bg-gray-50 pt-10">
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
          <div ref={messagesEndRef} />
        </div>

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

      <div className="mt-2 p-6">
        <div className="flex items-center mb-6">
          <div className="flex-grow border-b border-gray-300"></div>
          <p className="text-gray-400 mx-4 whitespace-nowrap">체크리스트를 작성하시면 더 정확한 결과를 얻으실 수 있습니다.</p>
          <div className="flex-grow border-b border-gray-300"></div>
        </div>
        <ChecklistForm onSubmit={handleChecklistSubmit} />
      </div>
    </div>
  );
};

export default DogImageAnalysisPage;