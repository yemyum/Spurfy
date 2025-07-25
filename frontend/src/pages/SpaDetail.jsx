import { useParams, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';
import StarRating from '../components/Common/StarRating';

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
    api.get(`/spa-services/slug/${spaSlug}`)
      .then((res) => setSpa(res.data.data))
      .catch(() => alert('상세정보 불러오기 실패🐽'));

    api.get('/dogs')
      .then((res) => setDogList(res.data.data))
      .catch(() => alert('강아지 목록 불러오기 실패🐶'));

    // 리뷰 데이터 가져오기!
        api.get(`/reviews/public/slug/${spaSlug}`)
            .then(res => {
                const fetchedReviews = res.data.data || [];
                setReviews(fetchedReviews);
                
                // ⭐ 평균 별점 계산 ⭐
                if (fetchedReviews.length > 0) {
                    const totalRating = fetchedReviews.reduce((sum, r) => sum + r.rating, 0);
                    setAverageRating(totalRating / fetchedReviews.length);
                } else {
                    setAverageRating(0);
                }
            })
            .catch(() => {});

  }, [spaSlug]);

  // [예약하기]는 결제페이지로 정보만 넘김!
  const handleReservation = () => {
    if (!selectedDogId || !date || !time) {
      alert("강아지, 날짜, 시간을 모두 선택해주세요! 🐶");
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

  if (!spa) return <div>로딩중…</div>;

  return (
    <div>
      <h2>{spa.name}</h2>
      <p>{spa.description}</p>
      <p>가격: {spa.price.toLocaleString()}원</p>
      <p>소요시간: {spa.durationMinutes || '미정'}분</p>
      <p>등록일: {spa.createdAt?.slice(0, 10)}</p>

      <hr />
      <h3>예약</h3>
      <select value={selectedDogId} onChange={(e) => setSelectedDogId(e.target.value)}>
        <option value="">강아지 선택</option>
        {dogList.map((d) => (
          <option key={d.dogId} value={d.dogId}>{d.name}</option>
        ))}
      </select>
      <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      
      {/* 시간 선택은 select, 가능한 시간대만! */}
      <select value={time} onChange={e => setTime(e.target.value)}>
        <option value="">시간 선택</option>
        {spa.availableTimes && spa.availableTimes.length > 0 ? (
          spa.availableTimes.map((t) => (
            <option key={t} value={t}>{t}</option>
          ))
        ) : (
          <option value="" disabled>가능한 시간이 없습니다</option>
        )}
      </select>
      <button onClick={handleReservation}>예약하기</button>
    <hr />
       <h3>서비스 리뷰 ({reviews.length}개)</h3> {/* 전체 리뷰 개수 표시 */}
            {/* ⭐ 평균 별점 표시 (선택 사항) ⭐ */}
            {reviews.length > 0 && (
                <div className="flex items-center mb-4">
                    <span className="font-semibold text-xl mr-2">총 평점: {averageRating.toFixed(1)}</span>
                    <StarRating rating={averageRating} readOnly={true} size="medium" />
                </div>
            )}
            <div>
        {reviews.length === 0 && <div>아직 리뷰가 없습니다!</div>}
        {reviews.slice(0, 4).map(r => (
          <div key={r.reviewId} className="border rounded p-3 mb-3">
            <div className="font-semibold">{r.userNickname}</div>
            <div className="mb-1">
                <StarRating rating={r.rating} readOnly={true} size="small" /> 
            </div>
            <div className="text-gray-700">{r.content}</div>
            {r.imageUrl && ( // 이미지가 있으면 보여주기
              <img src={r.imageUrl} alt="Review Image" className="max-w-full h-auto rounded-md mb-2" />
            )}
            <div className="text-xs text-gray-400">{r.createdAt?.slice(0,10)}</div>
          </div>
        ))}
        {reviews.length > 4 && ( // 4개보다 리뷰가 많을 때만 "더보기" 버튼 생성!
            <button
                onClick={() => navigate(`/spa-reviews/slug/${spaSlug}`)} // 새로운 리뷰 상세 페이지로 이동!
                className="w-full py-2 mt-4 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
                리뷰 전체 보기 ({reviews.length}개)
            </button>
        )}
      </div>
    </div>
  );
}

export default SpaDetail;