import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { BREED_OPTIONS } from '../constants/dogConstants';
import { GENDER_OPTIONS } from '../constants/dogConstants';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import SpurfyButton from '../components/Common/SpurfyButton';

function DogEdit() {
  const { dogId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    breed: '',
    birthDate: '',
    gender: '',
    weight: '',
    notes: '',
    imageUrl: '',
  });

  useEffect(() => {

    if (!dogId) {
      alert("강아지 정보를 불러올 수 없습니다. 다시 시도해주세요.");
      navigate('/mypage/dogs');
      return;
    }

    const fetchDogDetail = async () => {
      try {
        const res = await api.get(`/dogs/${dogId}`);
        if (res.data.code === 'S001') {
          const dog = res.data.data;
          setForm({
            ...dog,
            birthDate: dog.birthDate?.substring(0, 10), // 날짜 형식 YYYY-MM-DD로 맞추기
            notes: dog.notes ?? '', // null/undefined일 때 빈 문자열로 세팅
            imageUrl: dog.imageUrl ?? '', // null/undefined일 때 빈 문자열로 세팅
          });
        } else {
          // 서버에서 실패 코드를 보낼 경우 alert로 사용자에게 알림
          alert(res.data.message || '강아지 정보를 불러오는 데 실패했습니다.');
          setForm(null); // 실패 시 form 데이터 비움
          navigate('/mypage/dogs'); // 실패 시 목록으로 이동
        }
      } catch (err) {
        // 네트워크 오류 등 예외 발생 시 alert로 사용자에게 알림
        console.error('강아지 정보 불러오기 실패:', err);
        alert(err.response?.data?.message || '강아지 정보를 불러오는 데 실패했습니다.');
        setForm(null); // 에러 발생 시 form 데이터 비움
        navigate('/mypage/dogs'); // 에러 시 목록으로 이동
      }
    };
    fetchDogDetail();
  }, [dogId, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.patch(`/dogs/${dogId}`, form);
      if (res.data.code === 'S001') {
        alert('강아지 정보가 성공적으로 수정되었습니다!');
        navigate('/mypage/dogs');
      } else {
        alert(res.data.message || '강아지 정보 수정에 실패했습니다.');
      }
    } catch (err) {
      console.error('강아지 정보 수정 실패:', err);
      alert(err.response?.data?.message || '강아지 정보 수정에 실패했습니다.');
    }
  };

  if (!form.name) {
    return null; 
  }

  return (
    <div className="mx-auto p-8 mb-6 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">반려견 정보</h2>
      <form onSubmit={handleSubmit} className="border border-gray-200 py-6 rounded-md shadow-sm bg-white mb-6">

        <div className="flex flex-col md:flex-row gap-2 p-4">
          {/* ⭐ 1. 왼쪽: 이미지 영역과 사진 편집 버튼 ⭐ */}
          <div className="flex flex-col items-center flex-shrink-0 w-full md:w-1/3"> {/* flex-shrink-0로 이미지 영역 고정 폭, md:w-1/3으로 데스크탑에선 1/3폭 차지 */}
            <div className="w-44 h-44 bg-gray-200 rounded-lg overflow-hidden flex justify-center">
            </div>
            <button
              type="button" // 폼 제출 방지
              className="w-50 px-2 py-1 mt-2 text-gray-500 font-semibold rounded-md shadow-sm border border-gray-200 bg-white hover:bg-gray-50 transition duration-200"
              onClick={() => alert('사진 편집 기능 구현 예정!')} // alert 대신 모달 사용 권장
            >
              <FontAwesomeIcon icon={faCamera} /> 사진 편집하기
            </button>
          </div>

          {/* ⭐ 2. 오른쪽: 폼 입력 필드 영역 (flex-grow로 남은 공간 다 차지) ⭐ */}
          <div className="flex-grow mr-8"> {/* 폼 필드들을 담을 div */}
              <div className="space-y-6"> {/* 각 필드 사이에 간격 추가 */}
                {/* 1. 이름 */}
                <div className="flex items-center gap-2 border-b pb-5">
                  <label htmlFor="name" className="w-16 font-semibold text-lg text-gray-700 flex-shrink-0">이름</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    required
                    className="flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                    placeholder="반려견 이름"
                  />
                </div>

                {/* 2. 견종 */}
                <div className="flex items-center gap-2 border-b pb-5">
                  <label htmlFor="breed" className="w-16 font-semibold text-lg text-gray-700 flex-shrink-0">견종</label>
                  <select
                    id="breed"
                    name="breed"
                    value={form.breed}
                    onChange={handleChange}
                    required
                    className="flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                  >
                    <option value="">견종을 선택하세요</option>
                    {BREED_OPTIONS.map((breed) => (
                      <option key={breed} value={breed}>
                        {breed}
                      </option>
                    ))}
                  </select>
                </div>

                {/* 3. 생일 (캘린더 영역) */}
                <div className="flex items-center gap-2 border-b pb-5">
                  <label htmlFor="birthDate" className="w-16 font-semibold text-lg text-gray-700 flex-shrink-0">생일</label>
                  <input
                    type="date"
                    id="birthDate"
                    name="birthDate"
                    value={form.birthDate}
                    onChange={handleChange}
                    required
                    className="flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                  />
                </div>

                {/* 4. 성별 */}
                <div className="flex items-center gap-2 border-b pb-5">
                  <label htmlFor="gender" className="w-16 font-semibold text-lg text-gray-700 flex-shrink-0">성별</label>
                  <select
                    id="gender"
                    name="gender"
                    value={form.gender}
                    onChange={handleChange}
                    required
                    className="flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                  >
                    <option value="">성별을 선택하세요</option>
                    {GENDER_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* 5. 몸무게 */}
                <div className="flex items-center gap-2 border-b pb-5">
                  <label htmlFor="weight" className="w-16 font-semibold text-lg text-gray-700 flex-shrink-0">몸무게</label>
                  <input
                    type="number"
                    id="weight"
                    step="0.1"
                    name="weight"
                    placeholder="몸무게(kg)"
                    value={form.weight}
                    onChange={handleChange}
                    required
                    className="flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                  />
                </div>
              </div> {/* space-y-4 끝 */}

              {/* 6. 정보 작성란 (특이사항) */}
              <div className="mt-6">
                <label htmlFor="notes" className="block font-semibold text-lg text-gray-700 mb-2">특이사항</label>
                <textarea
                  id="notes"
                  name="notes"
                  placeholder="특이사항 (알레르기, 습관, 주의할 점 등)"
                  value={form.notes}
                  onChange={handleChange}
                  rows={5}
                  className="w-full p-2 border border-gray-300 rounded-md resize-y focus:outline-none focus:ring-2 focus:ring-gray-100"
                />
              </div>
          </div>
        </div>

      <div className="flex justify-end gap-3 mt-4 py-2 px-12">
        <SpurfyButton variant='primary' 
          type="submit"
          className="px-4 py-2">
           저장하기
        </SpurfyButton>
        <button
           type="button"
           className="px-4 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg shadow-sm hover:bg-gray-300 transition duration-300"
           onClick={() => navigate('/mypage/dogs')}
        >
           취소
        </button>
        </div>
      </form>
    </div>
  );
}

export default DogEdit;