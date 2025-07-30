import { useEffect, useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import SpurfyButton from '../components/Common/SpurfyButton';

function MyReservationList() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

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
    navigate(`/mypage/reservations/${reservationId}`);
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
        const finalCancelReason = cancelReasonInput.trim() === "" ? "사용자 요청" : cancelReasonInput;


        try {
            await api.post("/reservation/cancel", {
                reservationId: r.reservationId,
                cancelReason: finalCancelReason,
            });
            alert("예약이 취소되었습니다.");
            setReservations(prev => prev.filter(item => item.reservationId !== r.reservationId));
        } catch (err) {
            alert("예약 취소 실패: " + (err.response?.data?.message || "오류 발생"));
            console.error("예약 취소 오류:", err);
        }
    };

  if (loading) return <div className="p-6">로딩 중...</div>;
  if (reservations.length === 0) return <div className="p-6">예약 내역이 없습니다 🐶</div>;

  return (
    <div className="mx-auto p-8 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">예약 내역</h2>
      <h2 className="text-xl font-bold mb-6">나의 예약 리스트</h2>
      {reservations.map((r) => (
        <div
        key={r.reservationId}
        className="border border-gray-200 p-4 mb-4 rounded-md shadow-sm cursor-pointer hover:bg-blue-50 flex items-stretch gap-4"
         onClick={() => handleItemClick(r.reservationId)}
        >
  {/* ⭐ 1. 왼쪽: 이미지 영역 ⭐ */}
  <div className="w-24 h-24 bg-gray-200 rounded-lg flex-shrink-0 flex items-center justify-center overflow-hidden">
    <span className="text-gray-500 text-sm">이미지</span>
  </div>

  {/* ⭐ 2. 가운데: 예약 정보 텍스트 (서비스명, 날짜, 가격)⭐ */}
  <div className="flex-grow flex flex-col justify-between">
    <div> {/* 서비스명 */}
      <p className="text-lg font-bold text-gray-800">{r.serviceName}</p>
    </div>
    <div> {/* 날짜, 가격 */}
      <p className="text-gray-600 text-sm mb-1">{r.reservationDate} {r.reservationTime}</p>
      <p className="text-gray-900 font-bold text-lg">{r.price ? r.price.toLocaleString() : "가격 정보 없음"}원</p>
    </div>
  </div>

  {/* ⭐ 3. 오른쪽: 상태 태그와 버튼 영역 ⭐ */}
   <div className="flex flex-col justify-between items-end">
    {/* 예약 상태 태그! (맨 위 오른쪽) */}
    <div
      className={`inline-block px-2 py-1 rounded-full text-xs font-semibold ${statusLabel[r.reservationStatus]?.tagClass || 'bg-red-100 text-red-500'}`}
    >
      {statusLabel[r.reservationStatus]?.text || r.reservationStatus}
    </div>

  {/* 버튼 영역 (맨 아래 오른쪽) */}
  <div>
    {r.reservationStatus === "RESERVED" && (
      <SpurfyButton variant="danger"
        onClick={(e) => {
          e.stopPropagation();
          handleCancel(r);
        }}
        className="px-2 py-1 font-semibold text-sm"
      >
        예약취소
      </SpurfyButton>
    )}

    {r.reservationStatus === "COMPLETED" && !r.hasReview && (
      <SpurfyButton variant="ai"
        onClick={(e) => {
          e.stopPropagation();
          handleReviewWrite(r);
        }}
        className="px-2 py-1 font-semibold text-sm"
      >
        리뷰작성
      </SpurfyButton>
    )}
    </div>
  </div>
</div>
  ))}
</div>
  );
}

export default MyReservationList;