import { useParams, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';

function SpaDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [spa, setSpa] = useState(null);
  const [dogList, setDogList] = useState([]);
  const [selectedDogId, setSelectedDogId] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');

  useEffect(() => {
    api.get(`/spa-services/${id}`)
      .then((res) => setSpa(res.data.data))
      .catch(() => alert('상세정보 불러오기 실패🐽'));

    api.get('/dogs')
      .then((res) => setDogList(res.data.data))
      .catch(() => alert('강아지 목록 불러오기 실패🐶'));
  }, [id]);

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
    </div>
  );
}

export default SpaDetail;