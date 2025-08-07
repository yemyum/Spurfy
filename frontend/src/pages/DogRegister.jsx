import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { BREED_OPTIONS } from '../constants/dogConstants';
import { GENDER_OPTIONS } from '../constants/dogConstants';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import SpurfyButton from '../components/Common/SpurfyButton'

function DogRegister() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '',
    breed: '',
    customBreed: '',
    birthDate: '',
    gender: '',
    weight: '',
    notes: ''
  });

  // ⭐ 1. 새로운 상태 추가: 선택된 이미지 파일과 미리보기 URL
  const [dogImage, setDogImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  // ⭐ 2. 파일 선택 핸들러 함수
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setDogImage(file);
      // 이미지 미리보기를 위한 URL 생성
      setImagePreview(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const finalBreed = form.breed === '기타' ? form.customBreed.trim() : form.breed;

    if (!finalBreed) {
      alert('견종을 입력해주세요!');
      return;
    }

    // ⭐ 1. 백엔드 DTO에 맞는 객체를 프론트에서 생성
    const dogRequestDTO = {
        name: form.name,
        breed: finalBreed,
        birthDate: form.birthDate,
        gender: form.gender,
        weight: parseFloat(form.weight),
        notes: form.notes
    };
    
    // ⭐ 2. FormData 객체 생성
    const formData = new FormData();
    
    // ⭐ 3. JSON.stringify()를 이용해 DTO 객체를 문자열로 변환
    formData.append(
        'dogRequestDTO', 
        new Blob([JSON.stringify(dogRequestDTO)], { type: 'application/json' })
    );

    // ⭐ 4. 이미지 파일을 'dogImage'라는 키로 FormData에 추가
    if (dogImage) {
      formData.append('dogImage', dogImage);
    }
    
    try {
      // ⭐ 5. api.post의 두 번째 인수로 FormData를 전달
      await api.post('/dogs', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        }
      });

      alert('등록 완료!');
      navigate('/mypage/dogs');
    } catch (err) {
      console.error('등록 실패:', err);
      alert('오류가 발생하였습니다.');
    }
  };

  return (
    <div className="mx-auto p-8 mb-6 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">반려견 등록</h2>
      <form onSubmit={handleSubmit} className="w-full border border-gray-200 p-8 rounded-lg shadow-sm bg-white mb-6 break-words">

        <div className="flex flex-col md:flex-row">
          {/* ⭐ 1. 이미지 미리보기 영역과 파일 입력 버튼 ⭐ */}
          <div className="flex flex-col flex-shrink-0 w-full md:w-64">
            <div className="w-56 h-56 bg-gray-200 rounded-lg overflow-hidden flex justify-center items-center text-gray-400 text-sm"> {/* 이미지 미리보기 자리 */}
              {imagePreview ? (
                <img src={imagePreview} alt="Dog Preview" className="w-full h-full object-cover" />
              ) : (
                <span>사진 미리보기</span>
              )}
            </div>
            {/* 실제 파일 선택 input은 숨기고, 버튼을 클릭하면 input을 클릭하도록 연결 */}
            <input
              type="file"
              id="dogImage"
              name="dogImage"
              onChange={handleImageChange}
              className="hidden" // 화면에서 숨김
              accept="image/*" // 이미지 파일만 허용
            />
            <button
              type="button"
              className="w-56 px-2 py-1 mt-2 text-gray-500 font-semibold rounded-md shadow-sm border border-gray-200 bg-white hover:bg-gray-50 transition duration-300"
              onClick={() => document.getElementById('dogImage').click()} // 버튼 클릭 시 input 클릭
            >
              <FontAwesomeIcon icon={faCamera} /> 사진 선택하기
            </button>
          </div>

          {/* ⭐ 2. 오른쪽: 폼 입력 필드 영역 (flex-grow로 남은 공간 다 차지) ⭐ */}
          <div className="flex-grow">
            <div className="space-y-6">
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
                  {BREED_OPTIONS.map((breed) => ( // breedOptions 대신 BREED_OPTIONS로 변경했어!
                    <option key={breed} value={breed}>
                      {breed}
                    </option>
                  ))}
                </select>
              </div>

              {/* '기타' 견종 선택 시 직접 입력 필드 */}
              {form.breed === '기타' && (
                <div className="flex items-center gap-2 border-b pb-5">
                  <label htmlFor="customBreed" className="w-16 font-semibold text-lg text-gray-700 flex-shrink-0">직접 입력</label>
                  <input
                    type="text"
                    id="customBreed"
                    name="customBreed"
                    value={form.customBreed}
                    onChange={handleChange}
                    placeholder="예: 믹스견, 도사견 등"
                    required
                    className="flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                  />
                </div>
              )}

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
                  {GENDER_OPTIONS.map((option) => ( // GENDER_OPTIONS 사용!
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

        <div className="flex justify-between gap-3 mt-6">
          <button
            type="button"
            className="px-6 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg shadow-sm hover:bg-gray-300 transition duration-300"
            onClick={() => navigate('/mypage/dogs')}
          >
            취소
          </button>
          <SpurfyButton variant='primary'
            type="submit"
            className="px-4 py-2">
            등록하기 {/* 버튼 텍스트도 '등록하기'로 바꿨어! */}
          </SpurfyButton>
        </div>
      </form>
    </div>
  );
}

export default DogRegister;