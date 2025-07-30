import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import SpurfyButton from '../components/Common/SpurfyButton';

function MyReservationDetail() {
    const { reservationId } = useParams();
    const navigate = useNavigate();
    const [reservation, setReservation] = useState(null);
    const [loading, setLoading] = useState(true);

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

    const handleReviewWrite = () => {
        navigate(`/review/write?reservationId=${reservationId}`);
    };


    useEffect(() => {
        api.get(`/reservation/${reservationId}`) 
            .then(res => {
                setReservation(res.data.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("예약 상세 조회 실패:", err);
                alert("예약 상세 정보를 불러오는데 실패했습니다. 오류: " + (err.response?.data?.message || err.message));
                setLoading(false);
                navigate('/mypage/reservations');
            });
    }, [reservationId, navigate]);

    // ⭐ 예약 취소 처리 함수 수정 (prompt로 사유 입력받기) ⭐
    const handleCancel = async () => {
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

    if (loading) {
        return <div className="p-6 text-center">로딩 중...</div>;
    }

    if (!reservation) {
        return <div className="p-6 text-center">예약 정보를 찾을 수 없습니다 😢</div>;
    }

    const canCancel = reservation.reservationStatus === "RESERVED";

    return (
        <div className="mx-auto p-8 mb-6 select-none">
            <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">예약 상세</h2>

            <div className="border border-gray-200 py-6 rounded-md shadow-sm bg-white mb-6 relative">
                <span
                  className={`absolute top-4 right-4 inline-block px-3 py-1 rounded-full text-xs font-semibold ${statusLabel[reservation.reservationStatus]?.tagClass || 'bg-gray-100 text-gray-800'}`}
                >
                 {statusLabel[reservation.reservationStatus]?.text || reservation.reservationStatus}
                </span>
                <div className="pb-4 mb-4 border-b border-gray-200 px-6">
                <p className="text-lg font-semibold">예약 번호 {reservation.reservationId}</p>
                </div>

                <div className="pb-4 mb-4 border-b border-gray-200 px-6">
                <p className="text-lg mb-2">스파 서비스명: <span>{reservation.serviceName}</span></p>
                <p className="mb-2">반려견: {reservation.dogName}</p>
                <p className="mb-2">예약 일시: {reservation.reservationDate} {reservation.reservationTime}</p>
                </div>
                {/* 결제 수단 (paymentMethod) */}
                <div className="pb-4 mb-4 border-b border-gray-200 px-6">
                {reservation.paymentMethod && ( // paymentMethod 값이 있을 때만 표시
                <p className="mb-2">결제 수단: <span>{paymentMethodLabel[reservation.paymentMethod] || reservation.paymentMethod}</span></p>
                )}
                {/* 기존 가격 (서비스 자체의 정가) */}
                <p className="mb-2">서비스 가격: {reservation.price ? reservation.price.toLocaleString() : "정보 없음"}원</p>
                {/* 실제 결제 금액과 결제 수단 */}
                {/* 실제 결제 금액 (amount) */}
                {reservation.amount !== null && reservation.amount !== undefined && ( // amount 값이 있을 때만 표시
                <p className="mb-2">결제 금액: <span className="font-semibold text-spurfyBlue">{reservation.amount.toLocaleString()}원</span></p>
                )}
                {reservation.cancelReason && (
                    <p className="mb-2">취소 사유: {reservation.cancelReason}</p>
                )}
                {/* 환불 정보 */}
                {reservation.refundStatus && reservation.refundStatus !== "NONE" && (
                <p className="mb-2">환불 상태: <span className="font-semibold text-gray-500">{refundStatusLabel[reservation.refundStatus] || reservation.refundStatus}</span></p>
                )}
                {reservation.refundedAt && (
                <p className="mb-2">환불 일시: {new Date(reservation.refundedAt).toLocaleString()}</p>
                )}
            </div>

      {/* 세 번째 섹션: 버튼들 */}
      <div className="flex justify-end gap-3 mt-6 px-6">
        <button
          onClick={() => navigate(-1)}
          className="px-4 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg shadow-sm hover:bg-gray-300 transition duration-300"
        >
          뒤로가기
        </button>

        {/* 조건부 렌더링되는 버튼 (예약취소 or 리뷰작성) */}
        {canCancel && (
          <SpurfyButton variant = "danger"
            onClick={handleCancel}
            className="px-4 py-2 font-semibold transition duration-200"
          >
            예약취소
          </SpurfyButton>
        )}
        {reservation.reservationStatus === 'COMPLETED' && !reservation.hasReview && (
          <SpurfyButton variant = "ai"
            onClick={handleReviewWrite}
            className="px-4 py-2 font-semibold transition duration-200"
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