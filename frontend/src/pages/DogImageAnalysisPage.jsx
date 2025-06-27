import { useState } from "react";
import api from '../api/axios';

function DogImageAnalysisPage() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadMessage, setUploadMessage] = useState('');
  const [loading, setLoading] = useState(false);

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

  // 파일 업로드 + 분석 요청
  const handleImageAnalysis = async () => {
    if (!selectedFile) {
      alert('분석할 강아지 사진 파일을 선택해주세요!');
      return;
    }

    setLoading(true);
    setUploadMessage('사진 분석 요청 중...');

    const formData = new FormData();
    formData.append('dogImageFile', selectedFile);

    try {
      const response = await api.post('/dog-image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (response.data && response.data.message) {
        let msg = `성공: ${response.data.message}`;
        if (response.data.data) {
          msg += `\n결과: ${response.data.data}`;
        }
        setUploadMessage(msg);
      } else {
        setUploadMessage('이미지 분석은 성공했지만 응답 형식이 이상함!');
      }
      setSelectedFile(null);
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

  // 성공/오류 메시지 스타일 구분
  const isSuccess = uploadMessage.startsWith('성공');
  const messageBg = isSuccess
    ? "bg-green-50 border-green-400 text-green-800"
    : "bg-red-50 border-red-400 text-red-700";

  return (
    <div className="max-w-lg mx-auto mt-16 p-8 bg-white rounded-2xl shadow-lg border border-gray-100">
      <h2 className="text-2xl font-bold text-center text-gray-800 mb-2">
        강아지 사진 분석 & 스파 추천
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

      <button
        type="button"
        onClick={handleImageAnalysis}
        disabled={loading || !selectedFile}
        className={`w-full mt-6 py-3 rounded-xl shadow
          text-lg font-bold transition
          ${loading || !selectedFile
            ? "bg-gray-300 text-gray-500 cursor-not-allowed"
            : "bg-green-500 text-white hover:bg-green-600 active:scale-95"}`}
      >
        {loading ? '분석 중...' : '강아지 사진 분석 요청!'}
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