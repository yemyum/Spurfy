import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';
import SpurfyButton from '../components/Common/SpurfyButton';

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
      const res = await api.post('/reservation/pay', {
        dogId: state.dogId,
        serviceId: state.serviceId,
        reservationDate: state.date,
        reservationTime: state.time,
        amount: state.amount,
        paymentMethod: 'CARD'
      });

      const newId = res.data.data.reservationId;
      alert("✅ 결제 및 예약 완료! (돼지코 뽁🐽)");
      navigate(`/payment/${newId}`);
    } catch (err) {
      console.error(err);
      console.error("🐽 결제 에러:", err);
      console.log("🔍 서버 응답:", err.response?.data);  // 이거!!
      alert('결제 실패!');
    }
  };

  // 유저 정보 없을 때 로딩 표시
  if (!user) return <div>유저 정보 불러오는 중...</div>;

  // 결제 완료 후 상세 조회 페이지
  if (reservationId && reservation) {
    return (
      <div className="w-1/2 mx-auto select-none mt-10 mb-10 bg-white rounded-2xl shadow-md border border-gray-200">
        <div className="p-8 space-y-4">
        <h2 className="text-2xl font-bold mt-4 text-center mb-12">예약이 완료되었습니다!</h2>
        <div className="flex justify-between mb-2 text-lg">
        <span>스파 서비스명</span>
        <span className="font-semibold">{reservation.serviceName}</span>
        </div>
        <div className="flex justify-between mb-2 text-lg">
        <span>반려견</span>
        <span className="font-semibold">{reservation.dogName}</span>
        </div>
        <div className="flex justify-between mb-2 text-lg">
        <span>예약일시</span>
        <span className="font-semibold">{reservation.reservationDate} {reservation.reservationTime}</span>
        </div>
        <div className="border-t my-4 pt-4 flex justify-between mb-2 text-lg">
        <span>결제금액</span>
        <span className="font-semibold text-spurfyBlue">{reservation?.amount?.toLocaleString()}원</span>
        </div>
        </div>
        <div className="flex justify-center gap-4 p-8">
        <button
        className="flex-1 bg-gray-200 py-2 font-semibold text-lg rounded-lg hover:bg-gray-300"
        onClick={() => navigate('/')}
        >
        확인
        </button>
        <SpurfyButton variant="primary"
          className="flex-1 text-white py-2 text-lg rounded-lg"
          onClick={() => navigate('/mypage/reservations')}
        >
          내 예약 보러가기
        </SpurfyButton>
        </div>
      </div>
    );
  }

  // 결제 진행 화면
  return (
    <div className="w-2/3 mx-auto select-none mt-10 mb-10 bg-gray-50 rounded-2xl shadow-md overflow-hidden">
    <h2 className="bg-[#9EC5FF] text-white font-bold text-xl text-center py-4 rounded-t-2xl">결제하기</h2>
    <div className='p-4 space-y-6'>
      {/* 스파 서비스 정보 */}
    <div className="border-none rounded-md bg-white p-4">
      <h3 className="text-lg font-semibold">스파 서비스 정보</h3>
      <p>{state?.spaName || state?.serviceId}</p>
      <p>{state?.date} {state?.time}</p>
      <p>반려견: {state?.dogName || state?.dogId}</p>
      <p>가격: {state?.amount?.toLocaleString()}원</p>
    </div>

    {/* 결제자 정보 */}
    <div className="border-none rounded-md bg-white  p-4 space-y-2">
      <h3 className="text-lg font-semibold">결제자 정보</h3>
      <p>{user.name}</p>
      <p>{user.phone}</p>
    </div>

    {/* 결제수단 */}
    <div className="border-none rounded-md bg-white  p-4 space-y-2">
      <h3 className="text-lg font-semibold">결제수단</h3>
      <label className="flex items-center gap-2 cursor-pointer">
        <input type="radio" name="payment" defaultChecked className="accent-[#3B82F6] w-4 h-4" /> 카드
      </label>
      <label className="flex items-center gap-2 cursor-pointer">
        <input type="radio" name="payment" className="accent-[#3B82F6] w-4 h-4" /> 간편결제
      </label>
    </div>

    {/* 결제 금액 */}
    <div className="border-none rounded-md bg-white p-4 space-y-2">
      <h3 className="text-lg font-semibold">결제금액</h3>
      <div className="flex justify-between">
        <span>스파 서비스 금액</span>
        <span>{state?.amount?.toLocaleString()}원</span>
      </div>
      <div className="flex justify-between text-gray-400">
        <span>할인 금액</span>
        <span>- 0원</span>
      </div>
      <div className="flex justify-between text-gray-400">
        <span>쿠폰/포인트 할인</span>
        <span>- 0원</span>
      </div>
      <div className="border-t pt-2 flex justify-between font-bold text-spurfyBlue">
        <span>총 결제금액</span>
        <span>{state?.amount?.toLocaleString()}원</span>
      </div>
    </div>

    {/* 버튼 */}
    <SpurfyButton variant="primary"
      className="w-full text-white py-3 text-xl rounded-lg"
      onClick={handlePayment}
    >
      결제하기
    </SpurfyButton>
    </div>
  </div>
  );
}

export default PaymentPage;
