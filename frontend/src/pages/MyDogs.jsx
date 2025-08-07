import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import SpurfyButton from '../components/Common/SpurfyButton';

function MyDogs() {
  const navigate = useNavigate();
  const [dogs, setDogs] = useState([]);

  const fetchDogs = async () => {
    try {
      const res = await api.get('/dogs'); // 토큰 자동 실림
      console.log("🐶 강아지 리스트:", res.data.data);
      setDogs(res.data.data);
    } catch (err) {
      console.error('강아지 목록 조회 실패:', err);
      // alert('불러오기를 실패하였습니다.');
    }
  };

  useEffect(() => {
    fetchDogs();
  }, []);

  const handleDelete = async (dogId, dogName) => {
    if (!window.confirm(`정말 ${dogName}을(를) 삭제할까요?`)) return;

    try {
      await api.delete(`/dogs/${dogId}`);
      alert('삭제 완료!');
      setDogs(dogs.filter((dog) => dog.dogId !== dogId));
    } catch (err) {
      console.error('삭제 실패:', err);
      alert('삭제 중 오류가 발생했습니다!');
    }
  };

  return (
    <div className="mx-auto p-8 mb-6 select-none">
      <div className="text-2xl font-bold mb-6 text-spurfyBlue">반려견 케어</div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold">나의 반려견 리스트</h2>
       <SpurfyButton variant='outline' 
        className="whitespace-nowrap px-4 py-2 shadow-sm text-sm"
        onClick={() => navigate('/mypage/dogs/register')}>
        반려견 등록하기
       </SpurfyButton>
      </div>

      {dogs.length === 0 ? (
        <p>등록된 강아지가 없어요!</p>
      ) : (
        <ul>
          {dogs.map((dog) => (
            <li key={dog.dogId} 
                onClick={() => navigate(`/mypage/dogs/${dog.dogId}`)}
                className="border border-gray-200 p-4 mb-4 rounded-md shadow-sm cursor-pointer hover:bg-blue-50 flex flex-col gap-4">
                {/* ⭐ 1. 왼쪽: 이미지 영역 ⭐ */}
                <div className="flex items-stretch gap-4">
                <div className="w-28 h-28 bg-gray-200 rounded-lg flex-shrink-0 flex items-center justify-center overflow-hidden">
                    {dog.imageUrl ? (
                    <img 
                      src={`${import.meta.env.VITE_IMAGE_BASE_URL}${dog.imageUrl}`} 
                      alt={`${dog.name}의 사진`} 
                      className="w-full h-full object-cover" 
                    />
                  ) : (
                    <span className="text-gray-500 text-sm">이미지 없음</span>
                  )}
                </div>

                {/* ⭐ 2. 중간: 강아지 정보 영역 - flex-grow로 남은 공간 다 차지 ⭐ */}
                <div className="flex-grow flex flex-col mt-1">
                  <h3 className="font-semibold text-lg">{dog.name}</h3>
                  <p>견종: {dog.breed}</p>
                  <p>성별: {dog.gender === 'M' ? '남아' : '여아'}</p>
                </div>
               </div>

              {/* ⭐ 3. 오른쪽: 수정/삭제 버튼 영역 - 강아지 정보 영역 내부에 배치 ⭐ */}
              <div className="flex gap-2 self-start">
                <SpurfyButton variant="primary"
                    className="px-3 py-1 text-sm"
                    onClick={(e) => {
                    e.stopPropagation(); // 이벤트 버블링 방지!
                    navigate(`/mypage/dogs/${dog.dogId}/edit`);
                }}
                >
                수정
                </SpurfyButton>
                <SpurfyButton variant="danger"
                    className="px-3 py-1 text-sm"
                    onClick={(e) => {
                    e.stopPropagation();
                    handleDelete(dog.dogId, dog.name);
                   }}
                >
                  삭제
                </SpurfyButton>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default MyDogs;