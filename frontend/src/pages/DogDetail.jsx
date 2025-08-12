import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import SpurfyButton from '../components/Common/SpurfyButton';

function DogDetail() {
  const { dogId } = useParams(); // URL에서 dogId를 가져옴
  const navigate = useNavigate();
  const [dog, setDog] = useState(null);
  const [error, setError] = useState(null);

  const handleDelete = async () => {
    if (!dog) return; // dog 데이터가 없으면 실행하지 않음

    if (!window.confirm(`정말 ${dog.name}를(을) 삭제할까요?`)) {
      return;
    }

    try {
      await api.delete(`/dogs/${dog.dogId}`); // dog.dogId 사용
      alert('삭제 완료!');
      navigate('/mypage/dogs'); // 삭제 성공하면 반려견 리스트 페이지로 이동
    } catch (err) {
      console.error('삭제 실패:', err);
      alert('삭제 중 오류가 발생했습니다!');
    }
  };

  useEffect(() => {
    const fetchDogDetail = async () => {
      try {
        setError(null);
        const response = await api.get(`/dogs/${dogId}`);

        if (response.data.code === 'S001') {
          setDog(response.data.data);
        } else {
          setError(response.data.message || '강아지 상세 정보를 불러오는데 실패했습니다.');
        }
      } catch (err) {
        console.error("강아지 상세 정보 불러오기 에러:", err);
        setError(err.response?.data?.message || '네트워크 오류가 발생했습니다.');
      }
    };

    if (dogId) {
      fetchDogDetail();
    }
  }, [dogId]);

  if (error) {
    return <div className="p-4 text-red-600">에러 발생: {error}</div>;
  }

  // 아직 dog 데이터가 없으면 null 반환
  if (!dog) {
    return null;
  }

  return (

    <div className="mx-auto p-8 mb-6 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">반려견 정보</h2>

      <div className="border border-gray-200 py-6 rounded-md shadow-sm bg-white mb-6">

        <div className="pb-4 mb-4 border-b border-gray-200 px-6">
          <h2 className="text-2xl font-bold">
            <span className="bg-[#9EC5FF] text-white px-3 py-1 rounded-full shadow-sm text-xl">
              {dog.name}
            </span>{' '}
            의 상세 정보
          </h2>
        </div>

        {/* 1. 이미지와 상세 정보를 가로로 배치할 새로운 flex 컨테이너 */}
        <div className="flex items-start gap-6 px-6 pb-4 border-b border-gray-200">
          {/* ⭐ 1.1. 이미지 영역 ⭐ */}
          <div className="w-40 h-40 bg-gray-200 rounded-lg flex-shrink-0 flex items-center justify-center overflow-hidden">
            {dog.imageUrl ? (
              <img
                src={`${import.meta.env.VITE_IMAGE_BASE_URL}${dog.imageUrl}`}
                alt={`${dog.name} 이미지`}
                className="w-full h-full object-cover"
              />
            ) : (
              <span className="text-gray-500 text-sm">이미지 없음</span>
            )}
          </div>

          {/* ⭐ 1.2. 강아지 상세 정보 텍스트 영역 (flex-grow로 남은 공간 차지) ⭐ */}
          <div className="flex-grow flex flex-col">
            <p className="mb-1"><strong>견종:</strong> {dog.breed}</p>
            <p className="mb-1"><strong>생일:</strong> {dog.birthDate}</p>
            <p className="mb-1"><strong>성별:</strong> {dog.gender === 'M' ? '남아' : '여아'}</p>
            <p className="mb-1"><strong>몸무게:</strong> {dog.weight}kg</p>
            <p className="mb-4"><strong>특이사항:</strong> {dog.notes?.trim().length > 0 ? dog.notes : '없음'}</p>
          </div>
        </div>

        <div className="flex justify-between mt-6 px-6">
          <SpurfyButton variant='danger'
            onClick={handleDelete}
            className="px-4 py-2"
          >
            삭제하기
          </SpurfyButton>
          <SpurfyButton variant='primary'
            onClick={() => navigate(`/mypage/dogs/${dog.dogId}/edit`)}
            className="py-2 px-4"
          >
            수정하기
          </SpurfyButton>
        </div>
      </div>
    </div>
  );
}

export default DogDetail;