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

    const [chatMessages, setChatMessages] = useState([]);
    const [selectedFile, setSelectedFile] = useState(null);
    const [checklistData, setChecklistData] = useState(null);
    const [freeTextQuestion, setFreeTextQuestion] = useState('');
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    // 로컬 스토리지와 서버에서 메시지를 불러오는 로직
    useEffect(() => {
        const loadAndMergeMessages = async () => {
            console.log('⭐ [Load Start] 로컬 & 서버 메시지 불러오기 시작...');

            let serverAiMessages = [];
            try {
                const response = await api.get('/recommendations/history');
                if (response.data && response.data.data) {
                    serverAiMessages = response.data.data.map(item => {
                        let aiMessageText = '';
                        aiMessageText += `${item.intro || ''}\n`;
                        aiMessageText += `${item.compliment || ''}\n`;
                        if (item.recommendationHeader) {
                            aiMessageText += `${item.recommendationHeader}\n`;
                        }
                        if (item.spaName) {
                            aiMessageText += `${item.spaName.replace(/<\/?b>/g, '')}\n`;
                        }
                        if (item.spaDescription && item.spaDescription.length > 0) {
                            aiMessageText += item.spaDescription.join('\n');
                        }
                        aiMessageText += `\n${item.closing || ''}`;

                        const finalSpaSlug = item.spaSlug === "null" ? null : item.spaSlug;

                        return {
                            id: item.id,
                            text: aiMessageText,
                            isUser: false,
                            spaSlug: finalSpaSlug,
                            timestamp: new Date(item.createdAt).getTime()
                        };
                    });
                }
            } catch (error) {
                console.error('❌ 이전 AI 기록 불러오기 실패:', error);
            }
            console.log('⭐ [Load Step 1] 서버에서 불러온 AI 메시지:', serverAiMessages);

            // ⭐⭐⭐ [수정]: 로컬 스토리지에서 모든 메시지 불러오기! ⭐⭐⭐
            const savedLocalMessages = JSON.parse(localStorage.getItem("chatMessages")) || [];
            console.log('⭐ [Load Step 2] 로컬 스토리지에서 불러온 모든 메시지:', savedLocalMessages);

            // 로컬에 있는 메시지 중 서버 AI 메시지와 중복되지 않는 메시지만 필터링
            // (서버 메시지가 더 최신/정확하다고 가정)
            // ID를 기준으로 중복 제거 (서버 메시지는 ID가 있고, 로컬 사용자 메시지는 Date.now()로 생성되므로 겹칠 일 없음)
            const uniqueLocalMessages = savedLocalMessages.filter(localMsg => 
                localMsg.isUser || !serverAiMessages.some(serverMsg => serverMsg.id === localMsg.id)
            );
            console.log('⭐ [Load Step 3] 로컬에서 필터링한 중복 제거 메시지:', uniqueLocalMessages);


            const combinedMessages = [...uniqueLocalMessages, ...serverAiMessages];
            
            // ⭐⭐⭐ [수정]: 메시지 중복 제거 후 최종 병합 및 정렬 (최신 timestamp가 가장 아래로 오도록) ⭐⭐⭐
            const finalMessagesMap = new Map();
            combinedMessages.forEach(msg => {
                // ID가 없는 사용자 메시지나, ID가 있는 AI 메시지를 추가
                // 같은 ID의 메시지가 여러개라면, 나중에 추가된 메시지가 덮어쓰도록 함 (최신 데이터 유지)
                finalMessagesMap.set(msg.id, msg); 
            });

            const sortedFinalMessages = Array.from(finalMessagesMap.values()).sort((a, b) => a.timestamp - b.timestamp);
            
            setChatMessages(sortedFinalMessages);
            console.log('⭐ [Load End] 최종 합쳐진 메시지 (정렬 및 중복 제거 완료):', sortedFinalMessages);
        };

        loadAndMergeMessages();
    }, []); // 초기 로드 시에만 실행되도록 빈 배열 유지

    // chatMessages 상태가 변경될 때마다 로컬 스토리지에 저장
    // ⭐⭐⭐ [수정]: 이 useEffect는 필요 없음! 메시지 추가 함수에서 직접 저장하도록 변경함. ⭐⭐⭐
    // useEffect(() => {
    //     console.log('⭐ [Save] localStorage에 현재 chatMessages 저장:', chatMessages);
    //     localStorage.setItem("chatMessages", JSON.stringify(chatMessages));
    // }, [chatMessages]);

    // 채팅창 스크롤 로직
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
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

    // 메시지 추가 로직
    const addMessage = (text, isUser, spaSlug = null, id = Date.now(), timestamp = Date.now()) => { // id와 timestamp 인자 추가
        setChatMessages((prevMessages) => {
            const newMessage = {
                id, // 전달받은 id 사용
                text,
                isUser,
                spaSlug,
                timestamp // 전달받은 timestamp 사용
            };
            console.log('⭐ [Add] 새로운 메시지 추가:', newMessage);
            const newMessages = [...prevMessages, newMessage];
            // ⭐⭐⭐ [수정] 메시지를 추가한 후 즉시 로컬 스토리지에 저장! ⭐⭐⭐
            localStorage.setItem("chatMessages", JSON.stringify(newMessages));
            return newMessages;
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

        let userMessageText = '';
        if (selectedFile) userMessageText += `파일 선택: ${selectedFile.name}`;
        if (checklistData) userMessageText += `\n체크리스트: ${JSON.stringify(checklistData)}`;
        if (freeTextQuestion) userMessageText += `\n나의 질문: ${freeTextQuestion}`;
        
        // 사용자 메시지를 먼저 추가!
        // ⭐⭐⭐ [수정]: addMessage 함수를 사용하여 로컬 스토리지 자동 저장! ⭐⭐⭐
        addMessage(userMessageText, true);

        try {
            const formData = new FormData();
            formData.append('dogImageFile', selectedFile);

            if (checklistData) formData.append('checklist', JSON.stringify(checklistData));
            if (freeTextQuestion) formData.append('question', freeTextQuestion);

            const response = await api.post('/dog-image', formData, {
                headers: { 'Content-Type': 'multipart/form-ocata' }
            });

            if (response.data && response.data.data) {
                const aiResult = response.data.data;
                let aiMessageText = '';
                aiMessageText += `${aiResult.intro || ''}\n`;
                aiMessageText += `${aiResult.compliment || ''}\n`;
                if (aiResult.recommendationHeader) { 
                    aiMessageText += `${aiResult.recommendationHeader}\n`;
                }
                if (aiResult.spaName) {
                    aiMessageText += `${aiResult.spaName.replace(/<\/?b>/g, '')}\n`;
                }
                if (aiResult.spaDescription && aiResult.spaDescription.length > 0) {
                    aiMessageText += aiResult.spaDescription.join('\n');
                }
                aiMessageText += `\n${aiResult.closing || ''}`;

                const finalSpaSlug = aiResult.spaSlug === "null" ? null : aiResult.spaSlug;

                // ⭐⭐⭐ [수정]: addMessage 함수를 사용하여 로컬 스토리지 자동 저장! ⭐⭐⭐
                // 서버에서 받은 ID와 createdAt을 사용하여 메시지 추가
                addMessage(aiMessageText, false, finalSpaSlug, aiResult.id, new Date(aiResult.createdAt).getTime());

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
            // ⭐⭐⭐ [수정]: 에러 메시지도 addMessage로 추가해서 로컬 스토리지에 저장되도록! ⭐⭐⭐
            addMessage(`❌ AI 요청 실패: ${msg}`, false);
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
        <div className="w-full h-full mx-auto select-none bg-white mt-10 mb-10 overflow-hidden">
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
                        chatMessages.map((msg, i) => (
                            <div key={msg.id || i}>
                                <MessageBubble
                                    message={msg.text}
                                    isUser={msg.isUser}
                                    spaSlug={msg.spaSlug}
                                    onGoToSpaDetail={handleGoToSpaDetail}
                                />
                                {i === chatMessages.length - 1 && errorMessage && (
                                    <div className={`py-2 px-4 rounded-lg text-center whitespace-pre-wrap font-semibold ${messageBg} text-sm mt-6`}>
                                        {errorMessage}
                                    </div>
                                )}
                            </div>
                        ))
                    ) : (
                        <p className="text-center text-gray-500 p-20">AI 챗봇과 대화를 시작해보세요!</p>
                    )}
                </div>

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
                        <span className="text-xs text-gray-400 truncate max-w-[80px]">
                            {selectedFile.name}
                        </span>
                    )}

                    <textarea
                        id="freeTextQuestion"
                        rows="1"
                        value={freeTextQuestion}
                        onChange={(e) => setFreeTextQuestion(e.target.value)}
                        placeholder="우리 강아지에 대해 궁금한 점이 있나요?"
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
