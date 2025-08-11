import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import api from '../api/axios';
import ChecklistForm from "../components/Common/ChecklistForm";
import MessageBubble from "../components/Common/MessageBubble";
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import { faListCheck } from '@fortawesome/free-solid-svg-icons';
import SpurfyButton from "../components/Common/SpurfyButton";

const DogImageAnalysisPage = () => {
  const navigate = useNavigate();

  const messagesEndRef = useRef(null);
  const [chatMessages, setChatMessages] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const checklistDataRef = useRef(null);
  const [freeTextQuestion, setFreeTextQuestion] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

const [sheetOpen, setSheetOpen] = useState(false);
const [checklist, setChecklist] = useState({
  ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[]
});
const selectedCount = (checklist.selectedBreed ? 1 : 0)
  + (checklist.ageGroup ? 1 : 0)
  + (checklist.activityLevel ? 1 : 0)
  + (checklist.healthIssues?.length || 0);

const syncChecklistToRef = () => {
  // ref에 항상 최신 값 밀어넣기 (드로어 열려있든 닫혀있든 안전)
  checklistDataRef.current = {
    ...(checklistDataRef.current || {}),
    ...(checklist || {}),
  };
};

  // 진입 즉시 로그인 여부 확인 (사전 차단)
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인 후 이용 가능합니다.');
      navigate('/login');
    }
  }, [navigate]);

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
        console.error('AI 기록 불러오기 실패:', error);
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

  useEffect(() => {
  if (sheetOpen) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
  return () => { document.body.style.overflow = ''; };
}, [sheetOpen]);

  const handleChecklistSubmit = (data) => {
    console.log("체크리스트 데이터 변경:", data);
    checklistDataRef.current = data;
    setChecklist(data);              // ✅ 헤더 버튼 배지/표시용
  };

  const handleImageAnalysis = async (event) => {
    event.preventDefault();
    // 전송 직전에 한 번 더 안전하게 동기화
    syncChecklistToRef();

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
        text: `AI 요청 실패: ${msg}`,
        isUser: false,
      });
    }
  };

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
      {sheetOpen && (
  <>
    {/* 오버레이 */}
    <div className="fixed inset-0 bg-black/30 z-40" onClick={() => setSheetOpen(false)} />

    {/* 데스크톱: 우측 드로어 */}
    <div className="hidden md:block fixed right-0 top-0 h-full w-[460px] bg-black/80 z-50 shadow-2xl">
      <div className="p-4 border-b border-gray-500 flex items-center justify-between">
        <h3 className="font-semibold text-gray-200">체크리스트 작성</h3>
        <button onClick={() => setSheetOpen(false)} className="text-gray-500 font-semibold">닫기</button>
      </div>
      <div className="p-4 overflow-y-auto h-[calc(100%-56px)]">
        {/* ✅ 여기서 handleChecklistSubmit이 실제로 사용됨 */}
        <ChecklistForm onSubmit={handleChecklistSubmit} />
        <div className="mt-4 flex gap-2">
          <button
            onClick={() => {
              const empty = { ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[] };
              setChecklist(empty);
              checklistDataRef.current = empty;
            }}
            className="px-4 py-2 text-sm font-semibold bg-gray-500 text-gray-200 rounded-lg hover:bg-gray-600 transition duration-300"
          >
            초기화
          </button>
          <SpurfyButton variant="chat"
            onClick={() => {
              syncChecklistToRef();     // ✅ 최신 선택값 ref로 확정
              setSheetOpen(false);
            }}
            className="px-4 py-2 text-sm"
          >
            적용
          </SpurfyButton>
        </div>
      </div>
    </div>

    {/* 모바일: 바텀시트 */}
    <div className="md:hidden fixed inset-x-0 bottom-0 z-50 rounded-t-2xl bg-black/80 shadow-2xl">
      <div className="p-4 border-b border-gray-500 flex items-center justify-between">
        <h3 className="font-semibold text-gray-200">체크리스트 작성</h3>
        <button onClick={() => setSheetOpen(false)} className="text-gray-500 font-semibold">닫기</button>
      </div>
      <div className="p-4 max-h-[70vh] overflow-y-auto">
        {/* ✅ 모바일에서도 동일 */}
        <ChecklistForm onSubmit={handleChecklistSubmit} />
        <div className="mt-4 flex justify-end gap-2">
          <button
            onClick={() => {
              const empty = { ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[] };
              setChecklist(empty);
              checklistDataRef.current = empty;
            }}
            className="px-4 py-2 text-sm font-semibold bg-gray-500 text-gray-200 rounded-lg hover:bg-gray-600 transition duration-300"
          >
            초기화
          </button>
          <SpurfyButton variant="chat"
            onClick={() => {
              syncChecklistToRef();     // ✅ 최신 선택값 ref로 확정
              setSheetOpen(false);
            }}
            className="px-4 py-2 text-sm"
          >
            적용
          </SpurfyButton>
        </div>
      </div>
    </div>
  </>
)}
    </div>
  );
};

export default DogImageAnalysisPage;