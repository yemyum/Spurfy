import { useEffect, useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

function MyReservationList() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const statusLabel = {
    RESERVED: "예약완료",
    COMPLETED: "이용완료",
    CANCELED: "취소됨",
  };

  useEffect(() => {
    api.get("/reservation/mypage/reservations")
      .then(res => {
        setReservations(res.data.data || []);
        setLoading(false);
      })
      .catch(err => {
        console.error("예약 조회 실패:", err);
        setLoading(false);
      });
  }, []);

  const handleReviewWrite = (r) => {
    console.log("리뷰작성 눌렀을 때 넘길 정보:", r); 
    navigate("/review/write", {
      state: {
        reservationId: r.reservationId,
        dogId: r.dogId,
        serviceName: r.serviceName,
        dogName: r.dogName,
        reservationDate: r.reservationDate,
        price: r.price ?? 0, // 혹시 없을 경우 대비
      }
    });
  };

  const handleItemClick = (reservationId) => {
    navigate(`/mypage/reservations/${reservationId}`); // 예약 상세 페이지 경로로 이동!
};
  
  const handleCancel = async (r) => {
        // 1. 사용자에게 취소 의사를 한 번 더 확인 (Optional: 이중 확인)
        if (!window.confirm("정말 예약을 취소하시겠습니까?")) {
            return;
        }

        // 2. 취소 사유를 입력받음 (window.prompt 사용)
        //    사용자가 '취소'를 누를 경우 null이 반환됨
        //    사용자가 '확인'을 누르고 빈 값을 입력하면 빈 문자열이 반환됨
        const cancelReasonInput = window.prompt("예약을 취소하는 이유를 입력해주세요 (선택 사항):", "");

        // 사용자가 프롬프트에서 '취소'를 눌렀다면 함수 종료
        if (cancelReasonInput === null) {
            return;
        }

        // 3. cancelReason 설정 (사용자 입력 또는 기본값)
        //    prompt로 받은 값이 빈 문자열이더라도, 일단 넘기거나
        //    정말 아무 입력도 없으면 '사용자 요청'으로 설정 가능 (이건 선택 사항)
        const finalCancelReason = cancelReasonInput.trim() === "" ? "사용자 요청" : cancelReasonInput;


        try {
            await api.post("/reservation/cancel", {
                reservationId: r.reservationId,
                cancelReason: finalCancelReason, // ⭐ 사용자가 입력한 사유 또는 기본값 ⭐
            });
            alert("예약이 취소되었습니다.");
            // 상태 업데이트: 취소된 예약을 목록에서 제거
            setReservations(prev => prev.filter(item => item.reservationId !== r.reservationId));
        } catch (err) {
            alert("예약 취소 실패: " + (err.response?.data?.message || "오류 발생"));
            console.error("예약 취소 오류:", err); // 디버깅을 위해 콘솔에 에러 출력
        }
    };

  if (loading) return <div className="p-6">로딩 중...</div>;
  if (reservations.length === 0) return <div className="p-6">예약 내역이 없습니다 🐶</div>;

  return (
    <div className="p-6">
      <h2 className="text-xl font-bold mb-4">나의 예약 리스트</h2>
      {reservations.map((r) => (
        <div 
          key={r.reservationId} 
          className="border p-4 mb-4 rounded shadow-md cursor-pointer hover:bg-gray-50" // ⭐ 여기 추가! ⭐
          onClick={() => handleItemClick(r.reservationId)}
        >
          <p>🐾 <strong>{r.serviceName}</strong></p>
          <p>🐶 {r.dogName}</p>
          <p>🕒 {r.reservationDate} {r.reservationTime}</p>
          <p>💰 {r.price ? r.price.toLocaleString() : "가격 정보 없음"}원</p>
          <p>📌 상태: {statusLabel[r.reservationStatus] || r.reservationStatus}</p>

          {r.reservationStatus === "RESERVED" && (
            <button
               onClick={(e) => {
                            e.stopPropagation(); // 이벤트 버블링 막기!
                            handleCancel(r);
                        }}
              className="bg-gray-500 text-white px-3 py-1 rounded mt-2"
            >
              예약취소
            </button>
          )}

          {r.reservationStatus === "COMPLETED" && !r.hasReview && (
          <button
            onClick={(e) => {
                            e.stopPropagation();
                            handleReviewWrite(r);
                        }}
            className="bg-blue-500 text-white px-3 py-1 rounded mt-2"
          >
            리뷰작성
          </button>
        )}
        </div>
      ))}
    </div>
  );
}

export default MyReservationList;