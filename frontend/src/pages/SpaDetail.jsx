import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';
import StarRating from '../components/Common/StarRating';
import SpurfyButton from '../components/Common/SpurfyButton';
import welcomeSpa from '../assets/welcomeSpa.png';
import PremiumbrushingSpa from '../assets/PremiumbrushingSpa.png';
import RelaxingtherapySpa from '../assets/RelaxingtherapySpa.png';
import CalmingskinSpa from '../assets/CalmingskinSpa.png';

function SpaDetail() {
  const navigate = useNavigate();
  const location = useLocation();
  const { spaSlug } = useParams();

  const [spa, setSpa] = useState(null);
  const [dogList, setDogList] = useState([]);
  const [selectedDogId, setSelectedDogId] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [reviews, setReviews] = useState([]);
  const [averageRating, setAverageRating] = useState(0);
  const [serviceInfos, setServiceInfos] = useState([]);

  const spaDetailImageMap = {
    'welcome-spa': welcomeSpa,
    'premium-brushing-spa': PremiumbrushingSpa,
    'relaxing-therapy-spa': RelaxingtherapySpa,
    'calming-skin-spa': CalmingskinSpa,
  };

  useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        // 1) 공개 API 먼저
        const [spaRes, reviewRes] = await Promise.all([
          api.get(`/spa-services/slug/${spaSlug}`),
          api.get(`/reviews/public/slug/${spaSlug}?page=0&size=5`)
        ]);

        if (!mounted) return;

        const spaData = spaRes.data?.data || {};
        setSpa(spaData);

        const fetchedReviews = reviewRes.data?.data?.content || [];
        setReviews(fetchedReviews);
        setAverageRating(
          fetchedReviews.length
            ? fetchedReviews.reduce((sum, r) => sum + r.rating, 0) / fetchedReviews.length
            : 0
        );

        // 2) 토큰 있으면 /dogs 호출 (보호 API)
        const token = localStorage.getItem('token');
        if (token) {
          try {
            const dogRes = await api.get('/dogs');
            if (!mounted) return;
            setDogList(dogRes.data?.data || []);
          } catch (e) {
            console.warn('내 강아지 목록 불러오기 실패(로그인 필요/권한 문제):', e);
            setDogList([]); // 조용히 무시
          }
        } else {
          setDogList([]); // 비로그인: 빈 배열
        }

      } catch (e) {
        console.error("스파 상세/리뷰 로딩 에러:", e);
      }
    })();

    return () => { mounted = false; };
  }, [spaSlug]); // spaSlug가 변경될 때마다 useEffect 재실행되도록 의존성 배열에 추가

  useEffect(() => {
    (async () => {
      try {
        const res = await api.get('/service-info');
        setServiceInfos(Array.isArray(res.data) ? res.data : []);
      } catch (e) {
        console.warn('이용 안내 데이터 불러오기 실패:', e);
        setServiceInfos([]);
      }
    })();
  }, []);

  // [예약하기]는 결제페이지로 정보만 넘김!
  const handleReservation = () => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인 후 예약 가능합니다.');
      // 로그인 후 돌아오도록 리다이렉트 상태 심기
      navigate('/login', { state: { from: location.pathname } });
      return;
    }

    // 2) 로그인은 됐는데 필수값 누락
    if (!selectedDogId || !date || !time) {
      alert('강아지, 날짜, 시간을 모두 선택해주세요!');
      return;
    }

    // 3) 결제/예약 페이지로 이동
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
    <div className="w-full mx-auto bg-white rounded-xl shadow-md border border-gray-200 p-6">
      <div className="flex flex-col">
        {/* 1. 스파 사진 영역 (임시) */}
        <div className="w-full h-[220px] sm:h-[420px] bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden">
          {spa && spaDetailImageMap[spa.slug] ? (
            <img
              src={spaDetailImageMap[spa.slug]}
              alt={`${spa.name} 상세 이미지`}
              className="w-full h-[220px] sm:h-[420px] object-cover"
            />
          ) : (
            <span className="text-gray-400">이미지 준비중</span>
          )}
        </div>

        {/* 2. 이름 + 가격 */}
        <div className="py-4">
          <h2 className="text-2xl font-semibold text-spurfyBlue">{spa.name}
          </h2>
          <p className="font-semibold text-gray-800 text-lg">
            {spa.price.toLocaleString()}원
          </p>

          {/* 3. 스파 설명 */}
          <div className="bg-sky-50 border mt-3 border-sky-200 p-4 rounded-lg">
            <p className="text-gray-700 leading-relaxed whitespace-pre-line">
              {spa.description}
            </p>
          </div>
        </div>

        {/* 이용 전 안내 */}
        <div className="border-t border-gray-200 py-6">
          {serviceInfos.length === 0 ? (
            <p className="text-sm text-gray-500">준비 중이에요!</p>
          ) : (
            <ul className="space-y-4">
              {serviceInfos.map((info, idx) => {
                let emoji = '';

                switch (info.title) {
                  case '이용 시간 안내':
                    emoji = '🕒';
                    break;
                  case '이용 전 안내':
                    emoji = '📌';
                    break;
                  case '스파 서비스 소개':
                    emoji = '🛁';
                    break;
                  default:
                    emoji = '📄';
                }

                // ⭐ li 태그로 감싸고, key를 li에 추가 ⭐
                return (
                  <li key={idx}>
                    <h4 className={`font-semibold text-gray-700 mb-2`}>
                      {emoji} {info.title}
                    </h4>

                    {/* ⭐ info.content를 점리스트로 ⭐ */}
                    <ul className="list-disc list-inside space-y-2">
                      {info.content.split('\n').map((item, i) => (
                        <li key={i} className="text-sm text-gray-500">{item}</li>
                      ))}
                    </ul>
                  </li>
                );
              })}
            </ul>
          )}
          <p className="mt-4 text-sm font-semibold text-gray-500 whitespace-pre-wrap">
            ※ 피부 질환이나 심장 질환, 만성 질병이 있는 경우에는 반려견의 안전을 위해 전문가와 상담 후 이용해주시길 권장드립니다.
          </p>
        </div>

        {/* 4. 날짜 선택 */}
        <div className="border-t border-gray-200 py-6">
          <h3 className="font-semibold mb-2">이용 날짜 선택</h3>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100 p-2 w-1/2"
          />
        </div>

        {/* 5. 시간 선택 */}
        <div className="border-t border-gray-200 py-6">
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
        <div className="border-t border-gray-200 py-6">
          <h3 className="font-semibold">반려견 선택</h3>
          <p className="text-sm text-gray-400 mb-2">
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
        <div className="py-4">
          <SpurfyButton variant="primary"
            onClick={handleReservation}
            className="w-full text-white py-3 rounded font-semibold mb-2"
          >
            예약하기
          </SpurfyButton>
        </div>

        {/* 9. 리뷰 영역 */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold mb-2">리뷰 ({reviews.length}개)</h3>

          {/* 평균 별점 표시 */}
          {reviews.length > 0 && (
            <div className="flex items-center font-semibold text-lg mb-2">
              <span className="text-spurfyBlue text-xl mr-1">★</span>
              <span>{averageRating.toFixed(1)}</span>
            </div>
          )}

          {/* 리뷰 카드 3개 */}
          <div className="space-y-4">
            {reviews.slice(0, 3).map((r) => (
              <div key={r.reviewId} className="border rounded-lg p-4">
                <div className="font-semibold mb-1">{r.userNickname}</div>
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
          <div
            onClick={() => navigate(`/spa-reviews/slug/${spaSlug}`)} // 새로운 리뷰 상세 페이지로 이동!
            className="w-full text-center text-gray-400 mt-10 mb-2 cursor-pointer hover:underline"
          >
            더보기 ({reviews.length}개)
          </div>
        </div>
      </div>
    </div>
  );
}

export default SpaDetail;