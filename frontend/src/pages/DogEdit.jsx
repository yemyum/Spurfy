import React, { useEffect, useState, useRef} from 'react';
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
  const fileInputRef = useRef(null);

  const [form, setForm] = useState({
    name: '',
    breed: '',
    birthDate: '',
    gender: '',
    weight: '',
    notes: '',
    imageUrl: '',
  });

  const [selectedFile, setSelectedFile] = useState(null);
  const [previewImageUrl, setPreviewImageUrl] = useState(null);

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
            birthDate: dog.birthDate?.substring(0, 10),
            notes: dog.notes ?? '',
            imageUrl: dog.imageUrl ?? '',
          });
          if (dog.imageUrl) {
             // ⭐ 캐시 버스팅을 위한 타임스탬프 추가
             const imageUrlWithTimestamp = `${import.meta.env.VITE_IMAGE_BASE_URL}${dog.imageUrl}?t=${new Date().getTime()}`;
             setPreviewImageUrl(imageUrlWithTimestamp);
          }
        } else {
          alert(res.data.message || '강아지 정보를 불러오는 데 실패했습니다.');
          navigate('/mypage/dogs');
        }
      } catch (err) {
        console.error('강아지 정보 불러오기 실패:', err);
        alert(err.response?.data?.message || '강아지 정보를 불러오는 데 실패했습니다.');
        navigate('/mypage/dogs');
      }
    };
    fetchDogDetail();
  }, [dogId, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };
  
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      setPreviewImageUrl(URL.createObjectURL(file));
    }
  };
  
  const handleEditImageClick = () => {
    fileInputRef.current.click();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const dogUpdateRequestDTO = {
        name: form.name,
        breed: form.breed,
        birthDate: form.birthDate,
        gender: form.gender,
        weight: form.weight,
        notes: form.notes,
      };

      const formData = new FormData();
      
      const dtoBlob = new Blob([JSON.stringify(dogUpdateRequestDTO)], {
        type: 'application/json'
      });
      formData.append('dogUpdateRequestDTO', dtoBlob);

      if (selectedFile) {
        formData.append('dogImage', selectedFile);
      }

      const res = await api.patch(`/dogs/${dogId}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (res.data.code === 'S001') {
        alert('강아지 정보가 성공적으로 수정되었습니다!');

        // 성공적으로 수정되었으니 잠시 후 페이지 이동
        // 페이지를 이동하면 다시 강아지 정보를 불러오면서 최신화된 데이터를 보게 됨.
        setTimeout(() => {
          navigate('/mypage/dogs');
        }, 1000);

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
    <div className="mx-auto p-6 select-none">
      <h2 className="text-2xl font-semibold mb-8">반려견 정보 수정</h2>
      <form onSubmit={handleSubmit} className="w-full border-2 border-gray-100 p-6 rounded-xl shadow-sm bg-white mb-6 break-words">

        <div className="flex flex-col md:flex-row">
          <div className="flex flex-col flex-shrink-0 w-full md:w-64 mb-6 md:mb-0 md:mr-6">
            <div className="w-56 h-56 bg-gray-100 rounded-xl overflow-hidden flex justify-center items-center">
              {previewImageUrl ? (
                <img
                  src={previewImageUrl}
                  alt={`${form.name} 이미지`}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span className="text-gray-500 text-sm">이미지 없음</span>
              )}
            </div>
            
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              className="hidden"
              accept="image/*"
            />
            
            <button
              type="button"
              className="w-56 px-2 py-1 mt-2 text-gray-500 font-semibold rounded-md shadow-sm border-2 border-gray-100 bg-white hover:bg-gray-50 transition duration-300"
              onClick={handleEditImageClick}
            >
              <FontAwesomeIcon icon={faCamera} className="mr-2" />사진 편집하기
            </button>
          </div>

          <div className="flex-grow">
            <div className="space-y-6">
              <div className="flex items-center gap-2 border-b-2 border-gray-100 pb-5">
                <label htmlFor="name" className="w-16 font-semibold text-lg text-gray-500 flex-shrink-0">이름</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  required
                  className="flex-grow p-2 border-2 border-gray-100 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                  placeholder="반려견 이름"
                />
              </div>

              <div className="flex items-center gap-2 border-b-2 border-gray-100 pb-5">
                <label htmlFor="breed" className="w-16 font-semibold text-lg text-gray-500 flex-shrink-0">견종</label>
                <select
                  id="breed"
                  name="breed"
                  value={form.breed}
                  onChange={handleChange}
                  required
                  className="flex-grow p-2 border-2 border-gray-100 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                >
                  <option value="">견종을 선택하세요.</option>
                  {BREED_OPTIONS.map((breed) => (
                    <option key={breed} value={breed}>
                      {breed}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center gap-2 border-b-2 border-gray-100 pb-5">
                <label htmlFor="birthDate" className="w-16 font-semibold text-lg text-gray-500 flex-shrink-0">생일</label>
                <input
                  type="date"
                  id="birthDate"
                  name="birthDate"
                  value={form.birthDate}
                  onChange={handleChange}
                  required
                  className="flex-grow p-2 border-2 border-gray-100 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                />
              </div>

              <div className="flex items-center gap-2 border-b-2 border-gray-100 pb-5">
                <label htmlFor="gender" className="w-16 font-semibold text-lg text-gray-500 flex-shrink-0">성별</label>
                <select
                  id="gender"
                  name="gender"
                  value={form.gender}
                  onChange={handleChange}
                  required
                  className="flex-grow p-2 border-2 border-gray-100 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                >
                  <option value="">성별을 선택하세요.</option>
                  {GENDER_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center gap-2 border-b-2 border-gray-100 pb-5">
                <label htmlFor="weight" className="w-16 font-semibold text-lg text-gray-500 flex-shrink-0">몸무게</label>
                <input
                  type="number"
                  id="weight"
                  step="0.1"
                  name="weight"
                  placeholder="몸무게(kg)"
                  value={form.weight}
                  onChange={handleChange}
                  required
                  className="flex-grow p-2 border-2 border-gray-100 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
                />
              </div>
            </div>

            <div className="mt-6">
              <label htmlFor="notes" className="block font-semibold text-lg text-gray-500 mb-2">특이사항</label>
              <textarea
                id="notes"
                name="notes"
                placeholder="(알레르기, 습관, 주의할 점 등)"
                value={form.notes}
                onChange={handleChange}
                rows={5}
                className="w-full p-2 border-2 border-gray-100 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-gray-100"
              />
            </div>
          </div>
        </div>

      <div className="flex justify-between mt-6">
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
          저장하기
        </SpurfyButton>
        </div>
      </form>
    </div>
  );
}

export default DogEdit;