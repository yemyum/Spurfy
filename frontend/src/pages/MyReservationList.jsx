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

  
  const handleCancel = async (r) => {
    if (!window.confirm("정말 예약을 취소하시겠습니까?")) return;

    try {
      await api.post("/reservation/cancel", {
        reservationId: r.reservationId,
        cancelReason: "사용자 요청", // 필요 시 폼으로
      });
      alert("예약이 취소되었습니다.");
      setReservations(prev => prev.filter(item => item.reservationId !== r.reservationId));
    } catch (err) {
      alert("예약 취소 실패: " + (err.response?.data?.message || "오류 발생"));
    }
  };

  if (loading) return <div className="p-6">로딩 중...</div>;
  if (reservations.length === 0) return <div className="p-6">예약 내역이 없습니다 🐶</div>;

  return (
    <div className="p-6">
      <h2 className="text-xl font-bold mb-4">나의 예약 리스트</h2>
      {reservations.map((r) => (
        <div key={r.reservationId} className="border p-4 mb-4 rounded shadow-md">
          <p>🐾 <strong>{r.serviceName}</strong></p>
          <p>🐶 {r.dogName}</p>
          <p>🕒 {r.reservationDate} {r.reservationTime}</p>
          <p>💰 {r.price ? r.price.toLocaleString() : "가격 정보 없음"}원</p>
          <p>📌 상태: {statusLabel[r.reservationStatus] || r.reservationStatus}</p>

          {r.reservationStatus === "RESERVED" && (
            <button
              onClick={() => handleCancel(r)}
              className="bg-gray-500 text-white px-3 py-1 rounded mt-2"
            >
              예약취소
            </button>
          )}

          {r.reservationStatus === "COMPLETED" && (
            <button
              onClick={() => handleReviewWrite(r)}
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