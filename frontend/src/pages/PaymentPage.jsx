// 📁 src/pages/PaymentPage.jsx
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';

function PaymentPage() {
  const { reservationId } = useParams();           // 결제 완료 후 상세조회용 ID
  const { state } = useLocation();                 // 결제 이전 예약 정보
  const navigate = useNavigate();

  const [user, setUser] = useState(null);          // 유저 정보
  const [reservation, setReservation] = useState(null); // 결제완료 후 예약 상세

  // 💡 로그인 유저 정보 로딩
  useEffect(() => {
    api.get('/mypage/profile')
      .then((res) => setUser(res.data.data))
      .catch(() => alert('유저 정보 불러오기 실패 🐽'));
  }, []);

  // 💡 결제 완료 시 상세 조회
  useEffect(() => {
    if (reservationId) {
      api.get(`/reservation/${reservationId}`)
        .then((res) => setReservation(res.data.data))
        .catch(() => alert('예약 상세 조회 실패 🐶💥'));
    }
  }, [reservationId]);

  // 💳 결제 및 예약 등록
  const handlePayment = async () => {
    if (!state?.dogId || !state?.serviceId || !state?.date || !state?.time) {
      alert("예약 정보가 누락되었습니다. 🐽");
      return;
    }
    try {
      // 여기서 실제 결제 연동 가능 (실제 결제 성공했다고 가정)
      // const paymentResult = await 실제결제API();
      // if (!paymentResult.success) { ... }

      // 결제 성공 후 예약 등록
      const res = await api.post('/reservation', {
        dogId: state.dogId,
        serviceId: state.serviceId,
        reservationDate: state.reservationDate,
        reservationTime: state.reservationTime,
      });

      const newId = res.data.data.reservationId;
      alert("✅ 결제 및 예약 완료! (돼지코 뽁🐽)");
      navigate(`/payment/${newId}`);
    } catch (err) {
      console.error(err);
      alert("예약 저장 실패! 🐽💥");
    }
  };

  // 유저 정보 없을 때 로딩 표시
  if (!user) return <div>유저 정보 불러오는 중...</div>;

  // 결제 완료 후 상세 조회 페이지
  if (reservationId && reservation) {
    return (
      <div className="max-w-xl mx-auto mt-10 p-6 shadow-lg bg-white rounded-2xl">
        <h2 className="text-2xl font-bold mb-4">✅ 결제 완료!</h2>
        <p className="mb-2">🐶 강아지 이름: {reservation.dogName}</p>
        <p className="mb-2">💆 서비스명: {reservation.serviceName}</p>
        <p className="mb-2">📅 예약일시: {reservation.reservationDate} {reservation.reservationTime}</p>
        <p className="mb-2">🔖 예약 ID: {reservation.reservationId}</p>
        <button
          className="w-full py-2 mt-4 bg-purple-500 text-white rounded-2xl font-bold hover:bg-purple-600"
          onClick={() => navigate('/mypage/reservations')}
        >
          내 예약 목록 보기
        </button>
      </div>
    );
  }

  // 결제 진행 화면
  return (
    <div className="max-w-xl mx-auto mt-10 p-6 shadow-lg bg-white rounded-2xl">
      <h2 className="text-2xl font-bold mb-4">💳 결제 진행 중</h2>
      <p className="mb-2">🐶 강아지: {state?.dogName || state?.dogId}</p>
      <p className="mb-2">💆 서비스명: {state?.spaName || state?.serviceId}</p>
      <p className="mb-2">📅 예약일시: {state?.date} {state?.time}</p>
      <p className="mb-2">💸 결제금액: {state?.amount?.toLocaleString()}원</p>
      <button
        className="w-full py-2 bg-purple-500 text-white rounded-2xl font-bold hover:bg-purple-600"
        onClick={handlePayment}
      >
        결제하기
      </button>
    </div>
  );
}

export default PaymentPage;
