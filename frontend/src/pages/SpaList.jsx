import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import SpurfyButton from '../components/Common/SpurfyButton';

function SpaList() {
  const [list, setList] = useState([]);

  useEffect(() => {
    api.get('/spa-services')
      .then((res) => setList(res.data.data))
      .catch(() => alert('목록 불러오기 실패🐽'));
  }, []);

  return (
    <div className="w-full min-w-[1100px] max-w-[1280px] mx-auto mt-10 bg-white rounded-xl shadow-md border border-gray-200 p-10">
      <h2 className="text-2xl font-bold mb-8 text-spurfyBlue">스파 서비스</h2>

      <div className="grid grid-cols-2 lg:grid-cols-3 gap-8">
        {list.map((spa) => (
          <div
            key={spa.serviceId}
            className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 flex flex-col justify-between"
          >
            {/* 이미지 영역 */}
            <div className="h-40 bg-gray-100 rounded mb-4 flex items-center justify-center text-gray-400">
              이미지 준비 중
            </div>

            {/* 이름 + 가격 */}
            <div className="mb-2">
              <h3 className="text-lg font-semibold text-spurfyBlue">{spa.name}</h3>
              <p className="text-gray-800">가격: {spa.price.toLocaleString()} 원 ~</p>
            </div>

            {/* 태그 영역 (임시) */}
            <div className="flex flex-wrap gap-1 my-2">
              {spa.tags?.map((tag, index) => (
                <span key={index} className="text-xs bg-blue-50 text-blue-500 px-2 py-1 rounded-full">
                  #{tag}
                </span>
              ))}
            </div>

            {/* 예약 버튼 */}
            <Link to={`/spalist/slug/${spa.slug}`}>
            <SpurfyButton variant="primary" className="w-full text-center mt-2">
             예약하러 가기
            </SpurfyButton>
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}

export default SpaList;