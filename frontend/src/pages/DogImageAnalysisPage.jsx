import { useState } from "react";
import api from '../api/axios';
import ChecklistForm from "../components/Common/ChecklistForm";

function DogImageAnalysisPage() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadMessage, setUploadMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [checklistData, setChecklistData] = useState(null);
  const [freeTextQuestion, setFreeTextQuestion] = useState('');

  // 파일 선택 핸들러
  const handleFileChange = (event) => {
    if (event.target.files && event.target.files[0]) {
      setSelectedFile(event.target.files[0]);
      setUploadMessage('');
    } else {
      setSelectedFile(null);
      setUploadMessage('파일을 선택하지 않았습니다.');
    }
  };

  // 사용자 체크리스트 작성 (ChecklistForm에서 데이터가 넘어옴)
  const handleChecklistSubmit = (data) => {
    console.log("사용자가 선택한 체크리스트 값:", data);
    setChecklistData(data); // ChecklistForm에서 받은 데이터를 checklistData에 저장
  };


  // 파일 업로드 + 분석 요청
  const handleImageAnalysis = async () => {
    if (!selectedFile) {
      setUploadMessage('사진은 필수입니다.');
      return;
    }

    setLoading(true);
    setUploadMessage('사진 분석 요청 중...');

    const formData = new FormData();
    formData.append('dogImageFile', selectedFile);  // 사용자가 고른 이미지

    // ChecklistForm에서 넘어오는 data가 객체일 경우 JSON.stringify로 문자열화
    if (checklistData) {
      formData.append('checklist', JSON.stringify(checklistData));
    }

    if (freeTextQuestion) {
      formData.append('question', freeTextQuestion);
    }

    try {
      const response = await api.post('/dog-image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      // GPT 추천 내용만 표시
      if (response.data && response.data.data) {
        setUploadMessage(response.data.data);
      } else if (response.data && response.data.message) {
        setUploadMessage(`성공: ${response.data.message}`);
      } else {
        setUploadMessage('이미지 분석은 성공했지만 응답 형식이 이상함!');
      }
      setSelectedFile(null);
      setChecklistData(null);
      setFreeTextQuestion('');
    } catch (error) {
      let errorMessage = '이미지 분석 요청 중 오류가 발생했습니다!';
      if (error.response?.data?.message) {
        errorMessage = `오류: ${error.response.data.message} (${error.response.data.code})`;
      } else if (error.message) {
        errorMessage = `오류: ${error.message}`;
      }
      setUploadMessage(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 에러 메시지일 경우에만 빨간색 배경을 적용하도록 로직 변경
  const isError = uploadMessage.startsWith('오류:'); // '오류:'로 시작하면 에러 메시지
  const messageBg = isError
    ? "bg-red-50 border-red-400 text-red-700"
    : "bg-green-50 border-green-400 text-green-800"; // 그 외는 성공 또는 로딩 메시지로 간주

  return (
    <div className="max-w-lg mx-auto mt-16 p-8 bg-white rounded-2xl shadow-lg border border-gray-100">
      <h2 className="text-2xl font-bold text-center text-gray-800 mb-2">
        스퍼피 AI 봇
      </h2>
      <p className="text-center text-gray-500 mb-6 text-sm">
        분석할 강아지 사진을 업로드해주세요.
      </p>

      <label
        htmlFor="dogImageFileInput"
        className="block w-full text-center mb-3"
      >
        <input
          type="file"
          id="dogImageFileInput"
          accept="image/*"
          onChange={handleFileChange}
          disabled={loading}
          className="file:mr-3 file:py-2 file:px-4
                     file:rounded-full file:border-0
                     file:text-sm file:font-semibold
                     file:bg-green-100 file:text-green-700
                     hover:file:bg-green-200
                     border border-gray-200 rounded-lg py-2 px-3 w-full text-gray-700 cursor-pointer"
        />
      </label>

      {selectedFile && (
        <p className="text-center text-gray-600 text-sm mb-2">
          <span className="font-medium">선택된 파일:</span>{" "}
          <span className="font-semibold text-green-700">{selectedFile.name}</span>
          <span className="ml-2 text-xs text-gray-400">
            ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
          </span>
        </p>
      )}

    <div className="mt-10">
      <p className="mb-4">🧼 스파 추천 체크리스트</p>
      <ChecklistForm onSubmit={handleChecklistSubmit} />
    </div>

    <div className="mt-6">
          <label htmlFor="freeTextQuestion" className="block text-gray-700 font-semibold mb-2">
              🐶 우리 강아지에 대해 궁금한 점이 있나요? (선택 사항)
          </label>
          <textarea
              id="freeTextQuestion"
              rows="3"
              value={freeTextQuestion}
              onChange={(e) => setFreeTextQuestion(e.target.value)}
              placeholder="예: 우리 강아지는 피부가 민감한데, 어떤 스파가 좋을까요?"
              className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-300 transition duration-200"
              disabled={loading}
          ></textarea>
      </div>

      <button
        type="button"
        onClick={handleImageAnalysis}
        // loading 상태일 때만 disabled로 만들고,
        // selectedFile 유무와 상관없이 클릭 가능하게!
        disabled={loading}
        className={`w-full mt-6 py-3 rounded-xl shadow
          text-lg font-bold transition
          ${loading
            ? "bg-gray-300 text-gray-500 cursor-not-allowed"
            : "bg-green-500 text-white hover:bg-green-600 active:scale-95"}`}
      >
        {loading ? '분석 중...' : '추천 받기'}
      </button>

      {uploadMessage && (
        <div
          className={`mt-7 py-4 px-4 border-l-4 rounded-lg text-center whitespace-pre-wrap font-semibold shadow-sm ${messageBg}`}
        >
          {uploadMessage}
        </div>
      )}
    </div>
  );
}

export default DogImageAnalysisPage;