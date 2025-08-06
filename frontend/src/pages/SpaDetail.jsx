import { useParams, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';
import StarRating from '../components/Common/StarRating';
import SpurfyButton from '../components/Common/SpurfyButton';
import { formatDuration } from '../constants/formatDuration';

function SpaDetail() {
  const navigate = useNavigate();
  const { spaSlug } = useParams();
  const [spa, setSpa] = useState(null);
  const [dogList, setDogList] = useState([]);
  const [selectedDogId, setSelectedDogId] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [reviews, setReviews] = useState([]);
  const [averageRating, setAverageRating] = useState(0); 

  useEffect(() => {
  let mounted = true;

  (async () => {
    try {
      const [spaRes, dogRes, reviewRes] = await Promise.all([
        api.get(`/spa-services/slug/${spaSlug}`),
        api.get('/dogs'),
        api.get(`/reviews/public/slug/${spaSlug}`)
      ]);

      if (!mounted) return;

      setSpa(spaRes.data?.data || {});
      console.log("🔥 spa 데이터:", spaRes.data.data);
      console.log("🔥 availableTimes:", spaRes.data.data?.availableTimes);

      setDogList(dogRes.data?.data || []);
      
      const fetchedReviews = reviewRes.data?.data || [];
      setReviews(fetchedReviews);
      setAverageRating(
        fetchedReviews.length
          ? fetchedReviews.reduce((sum, r) => sum + r.rating, 0) / fetchedReviews.length
          : 0
      );
    } catch (e) {
        console.error("데이터 로딩 중 에러 발생:", e);
      } 
    })();

    return () => { mounted = false; };
  }, [spaSlug]); // spaSlug가 변경될 때마다 useEffect 재실행되도록 의존성 배열에 추가

  // [예약하기]는 결제페이지로 정보만 넘김!
  const handleReservation = () => {
    if (!selectedDogId || !date || !time) {
      alert("강아지, 날짜, 시간을 모두 선택해주세요!");
      return;
    }
    navigate('/payment', {
      state: {
        dogId: selectedDogId,
        serviceId: spa.serviceId,
        date,
        time,
        amount: spa.price,
        spaName: spa.name,
        dogName: dogList.find(d => d.dogId === selectedDogId)?.name || '',
      },
    });
  };

  if (!spa) {
    return null;
  }

  return (
    <div className="w-full mx-auto mt-10 mb-10 bg-white rounded-xl shadow-md border border-gray-200 p-6">
    <div className="flex flex-col space-y-4">
    {/* 1. 스파 사진 영역 (임시) */}
    <div className="h-64 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400">
      스파 이미지 (임시)
    </div>

    {/* 2. 스파 설명 */}
    <div className="bg-blue-50 p-4 rounded-lg">
      <p className="text-blue-900/50 font-semibold leading-relaxed whitespace-pre-line">
        {spa.description}
      </p>
    </div>

    {/* 3. 이름 + 가격 + 소요시간 */}
    <div className="mt-6">
      <h2 className="text-2xl font-bold text-spurfyBlue">{spa.name}</h2>
      {/* 가격 + 소요시간 */}
      <p className="font-semibold text-gray-800 text-lg">
        {spa.price.toLocaleString()}원 
        <span className="text-gray-500 text-base font-normal ml-1">
        ({formatDuration(spa.durationMinutes)})
        </span>
      </p>
    </div>

    {/* 3. 이용 전 안내 (임시) */}
    <div className="border-t border-gray-200 pt-4">
      <h3 className="font-semibold mb-2">이용 전 안내</h3>
      <p className="text-sm text-gray-500">※ 실제 안내 문구는 추후 추가 예정</p>
    </div>

    {/* 4. 날짜 선택 */}
    <div className="border-t border-gray-200 pt-4">
      <h3 className="font-semibold mb-2">날짜 선택</h3>
      <input
        type="date"
        value={date}
        onChange={(e) => setDate(e.target.value)}
        className="border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100 p-2 w-1/2"
      />
    </div>

    {/* 5. 시간 선택 */}
    <div className="border-t border-gray-200 pt-4">
      <h3 className="font-semibold mb-2">이용 시간 선택</h3>
      <select
        value={time}
        onChange={e => setTime(e.target.value)}
        className="border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100 p-2 w-1/2"
      >
        <option value="">시간 선택</option>
        {Array.isArray(spa.availableTimes) && spa.availableTimes.length > 0 ? (
        spa.availableTimes.map((t) => (
        <option key={t} value={t}>{t}</option>
        ))
        ) : (
        <option disabled>가능한 시간이 없습니다</option>
        )}
      </select>
    </div>

    {/* 6. 강아지 선택 */}
    <div className="border-t border-gray-200 pt-4">
      <h3 className="font-semibold">반려견 선택</h3>
      <p className="text-sm text-gray-500 mb-2">
        (반려견을 등록하셔야 예약이 가능하며, 등록하신 정보를 토대로 진행됩니다.)
      </p>
      <select
        value={selectedDogId}
        onChange={(e) => setSelectedDogId(e.target.value)}
        className="border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100 p-2 w-1/2"
      >
        <option value="">강아지 선택</option>
        {dogList.map((d) => (
          <option key={d.dogId} value={d.dogId}>{d.name}</option>
        ))}
      </select>
    </div>
      
    {/* 8. 예약 버튼 */}
    <div className="pt-6">
    <SpurfyButton variant="primary"
      onClick={handleReservation}
      className="w-full text-white py-3 rounded font-semibold"
    >
      예약하기
    </SpurfyButton>
    </div>

    {/* 9. 리뷰 영역 */}
    <div className="border-t border-gray-200 pt-4">
      <h3 className="text-xl font-bold mb-4">리뷰 ({reviews.length}개)</h3>
      
    {/* 평균 별점 표시 */}
      {reviews.length > 0 && (
        <div className="flex items-center font-semibold text-lg mb-4">
          <span className="text-spurfyBlue text-xl mr-1">★</span>
          <span>{averageRating.toFixed(1)}</span>
        </div>
      )}

    {/* 리뷰 카드 3개 */}
      <div className="space-y-4">
        {reviews.slice(0, 3).map((r) => (
          <div key={r.reviewId} className="border rounded-lg p-4">
            <div className="font-semibold mb-2">{r.userNickname}</div>
            <div className="flex items-center gap-2 mb-2">
              <StarRating rating={r.rating} readOnly={true} size="small" />
              <span className="text-sm text-gray-400">{r.createdAt?.slice(0, 10)}</span>
            </div>
            <div>{r.content}</div>
            {r.imageUrl && (
              <img 
                 src={r.imageUrl}
                   alt="Review" 
                   className="max-w-full h-auto rounded-md mt-2" 
                   loading="lazy"
               />
            )}
          </div>
        ))}
      </div>
        {reviews.length > 3 && ( // 4개보다 리뷰가 많을 때만 "더보기" 버튼 생성!
            <div
                onClick={() => navigate(`/spa-reviews/slug/${spaSlug}`)} // 새로운 리뷰 상세 페이지로 이동!
                className="w-full text-center text-sm text-gray-500 font-semibold mt-4 cursor-pointer hover:underline"
            >
                더보기 ({reviews.length}개)
            </div>
        )}
      </div>
    </div>
    </div>
  );
}

export default SpaDetail;