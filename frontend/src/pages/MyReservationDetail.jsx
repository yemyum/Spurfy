import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import SpurfyButton from '../components/Common/SpurfyButton';

function MyReservationDetail() {
  const { reservationId } = useParams();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState(null);

  const statusLabel = {
    RESERVED: {
      text: "예약완료",
      tagClass: "bg-sky-100 text-sky-500",
    },
    COMPLETED: {
      text: "이용완료",
      tagClass: "bg-green-100 text-green-500",
    },
    CANCELED: {
      text: "취소됨",
      tagClass: "bg-gray-100 text-gray-500",
    },
  };;

  const refundStatusLabel = {
    NONE: "해당없음",
    WAITING: "환불대기중",
    COMPLETED: "환불완료",
    FAILED: "환불실패",
  };

  const paymentMethodLabel = {
    CARD: "카드",
    EASY_PAY: "간편결제"
    //  "TRANSFER": "계좌이체", "KAKAO_PAY": "카카오페이" 등
  }

  const formatKoreanDateTime = (isoString) => {
    const date = new Date(isoString);
    const year = date.getFullYear();
    const month = date.getMonth() + 1; // 월은 0부터 시작
    const day = date.getDate();

    let hour = date.getHours();
    const minute = date.getMinutes().toString().padStart(2, '0');
    const second = date.getSeconds().toString().padStart(2, '0');

    const period = hour < 12 ? '오전' : '오후';
    if (hour > 12) hour -= 12;
    if (hour === 0) hour = 12;

    return `${year}. ${month}. ${day} ${period} ${hour}:${minute}:${second}`;
  };

  const handleReviewWrite = () => {
    // 1. reservation 객체가 있는지 먼저 확인 (안전 장치)
    if (!reservation) return;

    // 2. navigate의 두 번째 인자로 state 객체를 넘겨주기
    navigate("/review/write", {
      state: {
        reservationId: reservation.reservationId,
        dogId: reservation.dogId, // reservation 객체에서 직접 꺼내서 전달
        serviceName: reservation.serviceName,
        dogName: reservation.dogName,
        reservationDate: reservation.reservationDate,
        price: reservation.price ?? 0,
      }
    });
  };

  const handleCancel = async () => {
    // 방어 코드: reservation 데이터가 없을 경우를 대비
    if (!reservation) {
      console.error("Reservation data is missing when attempting to cancel.");
      alert("예약 정보를 불러오지 못해 취소할 수 없습니다. 다시 시도해주세요.");
      return;
    }

    const cancelReason = window.prompt("정말 예약을 취소하시겠습니까?\n취소 사유를 간단히 입력해주세요 (선택 사항):");

    // 사용자가 '취소'를 누르거나, 입력창을 비워둔 채 '확인'을 누를 수 있음
    if (cancelReason === null) { // 사용자가 취소 버튼을 눌렀을 경우
      return;
    }

    try {
      await api.post("/reservation/cancel", {
        reservationId: reservation.reservationId,
        cancelReason: cancelReason || "사용자 요청 (사유 미입력)", // 사유가 없으면 기본값
      });
      alert("예약이 성공적으로 취소되었습니다!");
      navigate('/mypage/reservations'); // 취소 후 예약 목록 페이지로 이동
    } catch (err) {
      console.error("예약 취소 실패:", err);
      alert("예약 취소 실패: " + (err.response?.data?.message || "알 수 없는 오류 발생"));
    }
  };

  useEffect(() => {
    // 1. reservationId가 없을 때의 안전 장치 추가
    if (!reservationId) {
      navigate('/mypage/reservations'); // 잘못된 접근으로 보고 목록으로 돌려보내기
      return;
    }

    const fetchReservationDetail = async () => {
      try {
        const res = await api.get(`/reservation/${reservationId}`);
        if (res.data.code === 'S001') {
          setReservation(res.data.data);
        } else {
          // 서버에서 실패 코드를 보낼 경우 alert로 사용자에게 알림
          alert(res.data.message || "예약 상세 정보를 불러오는데 실패했습니다.");
          setReservation(null); // 실패 시 데이터 비움
          navigate('/mypage/reservations'); // 실패 시 목록으로 이동
        }
      } catch (err) {
        // 네트워크 오류 등 예외 발생 시 alert로 사용자에게 알림
        alert("예약 상세 정보를 불러오지 못했습니다. 오류: " + (err.response?.data?.message || err.message));
        console.error("예약 상세 조회 실패:", err);
        setReservation(null); // 에러 시 데이터 비움
        navigate('/mypage/reservations'); // 에러 시 목록으로 이동
      }
    };
    fetchReservationDetail();
  }, [reservationId, navigate]); // (useCallback 훅을 사용하면 더 좋지만, 지금은 이렇게도 충분)

  if (!reservation) {
    return null;
  }

  // 예약 취소 가능 여부 (예약 상태가 "RESERVED"일 때만)
  const canCancel = reservation.reservationStatus === "RESERVED";
  // 리뷰 작성 가능 여부 (예약 상태가 "COMPLETED"일 때만)
  const canWriteReview = reservation.reservationStatus === "COMPLETED" && !reservation.hasReview;

  return (
    <div className="mx-auto p-8 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">예약 상세</h2>

      <div className="border-2 border-gray-100 py-5 rounded-xl shadow-sm bg-white mb-6 relative">
        <span
          className={`absolute top-4 right-4 inline-block px-3 py-1 rounded-full text-xs font-semibold ${statusLabel[reservation.reservationStatus]?.tagClass || 'bg-red-100 text-red-500'}`}
        >
          {statusLabel[reservation.reservationStatus]?.text || reservation.reservationStatus}
        </span>
        <div className="pb-4 mb-4 border-b-2 border-gray-100 px-6">
          <p className="text-xl font-semibold">
            <span className="text-gray-500">예약번호 | </span>
            <span className="text-lg font-medium">{reservation.reservationId}</span>
          </p>
        </div>

        <div className="pb-2 mb-4 border-b-2 border-gray-100 px-6">
          <p className="mb-4">
            <span className="text-gray-500">스파 서비스명 : </span>
            <span>{reservation.serviceName}</span>
          </p>
          <p className="mb-4">
            <span className="text-gray-500">이용 날짜 : </span>
            {reservation.reservationDate} {reservation.reservationTime}
          </p>
          <p className="mb-2">
            <span className="text-gray-500">반려견 : </span>
            <span>{reservation.dogName}</span>
          </p>
        </div>
        {/* 결제 수단 (paymentMethod) */}
        <div className="grid grid-cols-2 gap-x-8 mb-2 px-6">
          {reservation.paymentMethod && ( // paymentMethod 값이 있을 때만 표시
            <p className="mb-4">
              <span className="text-gray-500">결제 수단 : </span>
              <span>{paymentMethodLabel[reservation.paymentMethod] || reservation.paymentMethod}</span>
            </p>
          )}
          {reservation.createdAt && (
            <p className="mb-4">
              <span className="text-gray-500">결제 일시 : </span>
              <span>{formatKoreanDateTime(reservation.createdAt)}</span>
            </p>
          )}
          {/* 기존 가격 (서비스 자체의 정가) */}
          <p className="mb-2">
            <span className="text-gray-500">서비스 금액 : </span>
            <span>{reservation.price ? reservation.price.toLocaleString() : "정보 없음"}원</span>
          </p>
          {/* 실제 결제 금액과 결제 수단 */}
          {/* 실제 결제 금액 (amount) */}
          {reservation.amount !== null && reservation.amount !== undefined && ( // amount 값이 있을 때만 표시
            <p className="mb-2">
              <span className="text-gray-500">결제 금액 : </span>
              <span className="font-semibold text-spurfyBlue">{reservation.amount.toLocaleString()}원</span>
            </p>
          )}
        </div>
        {(reservation.cancelReason || reservation.refundStatus !== 'NONE' || reservation.refundedAt) && (
          <div className="pt-4 mb-4 px-6 border-t-2 border-gray-100">
            {reservation.cancelReason && (
              <p className="mb-4">
                <span className="text-gray-500">취소 사유 : </span>
                <span>{reservation.cancelReason}</span>
              </p>
            )}
            {/* 환불 정보 */}
            {reservation.refundStatus && reservation.refundStatus !== "NONE" && (
              <p className="mb-4">
                <span className="text-gray-500">환불 상태 : </span>
                <span className="font-semibold">{refundStatusLabel[reservation.refundStatus] || reservation.refundStatus}</span>
              </p>
            )}
            {reservation.refundedAt && (
              <p className="mb-2">
                <span className="text-gray-500">환불 일시 : </span>
                <span>{formatKoreanDateTime(reservation.refundedAt)}</span>
              </p>
            )}
          </div>
        )}

        {/* 세 번째 섹션: 버튼들 */}
        <div className="flex justify-between pt-5 px-6 border-t-2 border-gray-100">
          <button
            onClick={() => navigate(-1)}
            className="px-4 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg hover:bg-gray-300 transition duration-300"
          >
            뒤로가기
          </button>

          {/* 조건부 렌더링되는 버튼 (예약취소 or 리뷰작성) */}
          {canCancel && (
            <SpurfyButton variant="danger"
              onClick={handleCancel}
              className="px-4 py-2"
            >
              예약취소
            </SpurfyButton>
          )}
          {canWriteReview && (
            <SpurfyButton variant="ai"
              onClick={handleReviewWrite}
              className="px-4 py-2"
            >
              리뷰 작성하기
            </SpurfyButton>
          )}
          {/* 이용 완료되거나 취소된 예약에 대한 메시지 (버튼 대신 표시) */}
          {/* {reservation.reservationStatus === 'COMPLETED' && !reservation.hasReview && (
        // <p className="text-gray-600 text-sm italic">이용 완료된 예약입니다.</p>
        )}
        {reservation.reservationStatus === 'CANCELED' && (
        // <p className="text-gray-600 text-sm italic">이미 취소된 예약입니다.</p>
      )} */}
        </div>
      </div>
    </div>
  );
}

export default MyReservationDetail;