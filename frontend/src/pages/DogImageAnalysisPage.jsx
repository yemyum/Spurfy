import { useState, useEffect, useRef } from "react";
import api from '../api/axios';
import ChecklistForm from "../components/Common/ChecklistForm";
import MessageBubble from "../components/Common/MessageBubble";
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import SpurfyButton from "../components/Common/SpurfyButton";

const DogImageAnalysisPage = () => {
    const navigate = useNavigate();
    const chatContainerRef = useRef(null);
    const messagesEndRef = useRef(null);

    const [chatMessages, setChatMessages] = useState([]);
    const [selectedFile, setSelectedFile] = useState(null);
    const [checklistData, setChecklistData] = useState(null);
    const [freeTextQuestion, setFreeTextQuestion] = useState('');
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    // 줄바꿈 변환 유틸 (백엔드에서 온 개행을 통일)
    const normalizeNewLines = (text) => {
      if (!text) return '';
      return text
        .replace(/\\n/g, '\n')  // JSON 이스케이프된 개행 처리
        .replace(/\r\n/g, '\n') // 윈도우 개행 처리
        .trim();
    };

    // 공통 AI 응답 포맷터
    const formatAiMessage = (result) => {
    const intro = normalizeNewLines(result.intro);
    const compliment = normalizeNewLines(result.compliment);
    const recommendationHeader = normalizeNewLines(result.recommendationHeader);
    const spaName = normalizeNewLines(result.spaName);
    const closing = normalizeNewLines(result.closing);

    const spaDescription = (result.spaDescription && result.spaDescription.length > 0)
      ? result.spaDescription.map(line => `- ${normalizeNewLines(line).replace(/^- /, '')}`).join('\n')
      : '';

    const finalSpaSlug = result.spaSlug === "null" ? null : result.spaSlug;

    return {
      text: [
        intro,
        compliment,
        recommendationHeader,
        spaName,
        spaDescription,
        closing
      ] 
       .filter(Boolean)
       .join('\n\n'),
      spaSlug: finalSpaSlug,
      id: result.id,
      timestamp: new Date(result.createdAt).getTime()
     };
};

    // 로컬 스토리지와 서버에서 메시지를 불러오는 로직
    useEffect(() => {
        const loadAndMergeMessages = async () => {
            console.log('⭐ [Load Start] 로컬 & 서버 메시지 불러오기 시작...');

            let serverAiMessages = [];
            try {
                const response = await api.get('/recommendations/history');
                if (response.data && response.data.data) {
                    serverAiMessages = response.data.data.map(item => ({
                        ...formatAiMessage(item),
                        isUser: false
                    }));
                }
            } catch (error) {
                console.error('❌ 이전 AI 기록 불러오기 실패:', error);
            }
            console.log('⭐ [Load Step 1] 서버에서 불러온 AI 메시지:', serverAiMessages);

            const savedLocalMessages = JSON.parse(localStorage.getItem("chatMessages")) || [];
            console.log('⭐ [Load Step 2] 로컬 스토리지에서 불러온 모든 메시지:', savedLocalMessages);

            const localMessagesWithBase64 = await Promise.all(savedLocalMessages.map(async msg => {
                if (!msg.text && msg.message) {
                    msg.text = msg.message;
                    delete msg.message;            
                }
                if (msg.isUser && msg.imageUrl && msg.imageUrl.startsWith("blob:")) {
                    return { ...msg, imageUrl: null };
                }
                if (!msg.isUser) {
                    return { ...formatAiMessage(msg), isUser: false };
                }
                return msg;
            }));

            const uniqueLocalMessages = localMessagesWithBase64.filter(localMsg => localMsg.isUser);

            console.log('⭐ [Load Step 3] 로컬에서 필터링한 중복 제거 메시지:', uniqueLocalMessages);

            const combinedMessages = [...uniqueLocalMessages, ...serverAiMessages];
            
            const finalMessagesMap = new Map();
            combinedMessages.forEach(msg => {
                finalMessagesMap.set(msg.id, msg); 
            });

            const sortedFinalMessages = Array.from(finalMessagesMap.values()).sort((a, b) => a.timestamp - b.timestamp);
            
            setChatMessages(sortedFinalMessages);
            console.log('⭐ [Load End] 최종 합쳐진 메시지:', sortedFinalMessages);

            if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
            }
        };

        loadAndMergeMessages();
    }, []);

    useEffect(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: "auto" });
    }, [chatMessages]);

    const handleFileChange = (event) => {
        if (event.target.files && event.target.files[0]) {
            setSelectedFile(event.target.files[0]);
            setErrorMessage('');
        } else {
            setSelectedFile(null);
            setErrorMessage('파일을 선택하지 않았습니다.');
        }
    };

    const handleChecklistSubmit = (data) => {
        console.log("사용자가 선택한 체크리스트 값:", data);
        setChecklistData(data);
    };

    // addMessage 개선 (기존 ID 유지)
    const addMessage = (newMessageObj) => { 
        setChatMessages((prevMessages) => {
            const newMessage = {
                id: newMessageObj.id || Date.now(),
                timestamp: newMessageObj.timestamp || Date.now(),
                ...newMessageObj,
            };
            if (!newMessage.text && newMessage.message) {
                newMessage.text = newMessage.message;
                delete newMessage.message;
            }
            const newMessages = [...prevMessages, newMessage];
            localStorage.setItem("chatMessages", JSON.stringify(newMessages));
            return newMessages;
        });
    };

    const getBase64 = (file) => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = (error) => reject(error);
        });
    };

    const handleImageAnalysis = async (event) => {
        event.preventDefault();

        if (!selectedFile) {
            setErrorMessage('사진은 필수입니다. 파일을 선택해주세요!');
            return;
        }

        setLoading(true);
        setErrorMessage('');

        const userImageUrl = await getBase64(selectedFile);
        const userMessageId = Date.now();

        addMessage({ 
            id: userMessageId,
            text: freeTextQuestion,
            isUser: true, 
            imageUrl: userImageUrl,
            checklist: checklistData,
        });

        try {
            const formData = new FormData();
            formData.append('dogImageFile', selectedFile);
            if (checklistData) formData.append('checklist', JSON.stringify(checklistData));
            if (freeTextQuestion) formData.append('question', freeTextQuestion);

            const response = await api.post('/dog-image', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            if (response.data && response.data.data) {
                const aiResult = response.data.data;
                const formattedMessage = formatAiMessage(aiResult);

                addMessage({
                    text: formattedMessage.text,   // 여기 text는 '\n' 포함
                    isUser: false,
                    spaSlug: formattedMessage.spaSlug,
                    id: formattedMessage.id,
                    timestamp: formattedMessage.timestamp
                });

                setErrorMessage('');
            } else {
                setErrorMessage('이미지 분석은 성공했지만 응답 형식이 이상합니다!');
            }

            setSelectedFile(null);
            setChecklistData(null);
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
        } finally {
            setLoading(false);
        }
    };

    const handleGoToSpaDetail = (spaSlug) => {
        if (spaSlug) {
            navigate(`/spalist/slug/${spaSlug}`);
        }
    };
    
    const messageBg = errorMessage ? "bg-red-50 text-red-600" : "";

    return (
        <div className="w-full h-full mx-auto bg-white mt-10 mb-10 overflow-hidden">
            <div className="fixed top-0 left-0 right-0 z-50 bg-black/80 p-4 shadow-lg flex justify-center items-center">
                <h2 className="text-2xl font-bold text-spurfyAI">
                    Spurfy AI Chat
                </h2>
            </div>
            
            <div className="flex-1 relative bg-gray-50 pt-10">
                <div
                    ref={chatContainerRef}
                    className="flex-1 bg-gray-50 overflow-y-auto p-6 pb-24 flex flex-col space-y-2"
                >
                    {chatMessages.length > 0 ? (
                        chatMessages.map((msg, i) => {
                            const normalizedText = normalizeNewLines(msg.text);
                            return (
                            <MessageBubble
                                key={msg.id || i}
                                text={normalizedText}
                                isUser={msg.isUser}
                                imageUrl={msg.imageUrl}
                                checklist={msg.checklist}
                                spaSlug={msg.spaSlug}
                                onGoToSpaDetail={handleGoToSpaDetail}
                            />
                            );
                        })
                    ) : (
                        <p className="text-center text-gray-500 p-20">AI 챗봇과 대화를 시작해보세요!</p>
                    )}

                    {/* 에러 메시지 항상 출력 */}
                    {errorMessage && (
                        <div className={`py-2 px-4 rounded-lg text-center whitespace-pre-wrap font-semibold ${messageBg} text-sm`}>
                            {errorMessage}
                        </div>
                    )}
                    <div ref={messagesEndRef} />
                </div >

                <form 
                    onSubmit={handleImageAnalysis} 
                    className="absolute bottom-0 left-0 right-0 z-40 w-full flex items-center gap-4 p-4 bg-gray-100"
                >
                    <label htmlFor="dogImageFileInput" className="cursor-pointer">
                        <input
                            type="file"
                            id="dogImageFileInput"
                            accept="image/*"
                            onChange={handleFileChange}
                            disabled={loading}
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
                        disabled={loading}
                        onInput={(e) => {
                            e.target.style.height = 'auto';
                            e.target.style.height = e.target.scrollHeight + 'px';
                        }}
                    ></textarea>

                    <SpurfyButton variant="chat"
                        type="submit"
                        disabled={loading}
                        className={`py-2 px-4 text-sm font-semibold
                            ${loading ? "cursor-not-allowed" : " "}`}
                    >
                        {loading ? '전송 중' : '전송'}
                    </SpurfyButton>
                </form>
            </div>

            <div className="mt-2 p-6">
                <div className="flex items-center mb-6">
                    <div className="flex-grow border-b border-gray-300"></div>
                    <p className="text-gray-400 mx-4 whitespace-nowrap">
                        체크리스트를 작성하시면 더 정확한 결과를 얻으실 수 있습니다.
                    </p>
                    <div className="flex-grow border-b border-gray-300"></div>
                </div>
                <ChecklistForm onSubmit={handleChecklistSubmit} />
            </div>
        </div>
    );
}

export default DogImageAnalysisPage;