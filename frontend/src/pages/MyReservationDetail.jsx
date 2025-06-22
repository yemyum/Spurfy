import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";

function MyReservationDetail() {
    const { reservationId } = useParams();
    const navigate = useNavigate();
    const [reservation, setReservation] = useState(null);
    const [loading, setLoading] = useState(true);

    const statusLabel = {
        RESERVED: "예약완료",
        COMPLETED: "이용완료",
        CANCELED: "취소됨",
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
            await api.post("/api/reservation/cancel", { 
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
    // ⭐⭐

    if (loading) {
        return <div className="p-6 text-center">로딩 중...</div>;
    }

    if (!reservation) {
        return <div className="p-6 text-center">예약 정보를 찾을 수 없습니다 😢</div>;
    }

    const canCancel = reservation.reservationStatus === "RESERVED";

    return (
        <div className="p-6 max-w-2xl mx-auto bg-white shadow-md rounded-lg">
            <h2 className="text-2xl font-bold mb-6 text-center">나의 예약 상세 정보</h2>
            
            <div className="border border-gray-200 p-6 rounded-md bg-gray-50 mb-6">
                <p className="text-lg mb-2">🐾 서비스: <span className="font-semibold">{reservation.serviceName}</span></p>
                <p className="mb-2">🐶 강아지: {reservation.dogName}</p>
                <p className="mb-2">🕒 예약 일시: {reservation.reservationDate} {reservation.reservationTime}</p>
                <p className="mb-2">💰 결제 금액: {reservation.price ? reservation.price.toLocaleString() : "가격 정보 없음"}원</p>
                <p className="mb-2">📌 예약 상태: <span className="font-medium text-blue-600">{statusLabel[reservation.reservationStatus] || reservation.reservationStatus}</span></p>
                {reservation.cancelReason && (
                    <p className="text-red-500 mt-2">❗ 취소 사유: {reservation.cancelReason}</p>
                )}
            </div>

            <div className="flex justify-between items-center mt-6">
                {canCancel ? (
                    <button
                        onClick={handleCancel}
                        className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded transition duration-200"
                    >
                        예약 취소하기
                    </button>
                ) : (
                    <>
                        {reservation.reservationStatus === 'COMPLETED' && (
                            <p className="text-gray-600 text-sm italic">이용 완료된 예약은 취소할 수 없습니다.</p>
                        )}
                        {reservation.reservationStatus === 'CANCELED' && (
                            <p className="text-gray-600 text-sm italic">이미 취소된 예약입니다.</p>
                        )}
                    </>
                )}
                
                <button
                    onClick={() => navigate(-1)}
                    className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded transition duration-200"
                >
                    목록으로 돌아가기
                </button>
            </div>
        </div>
    );
}

export default MyReservationDetail;