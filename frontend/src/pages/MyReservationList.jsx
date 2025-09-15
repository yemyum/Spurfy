import { useEffect, useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import SpurfyButton from '../components/Common/SpurfyButton';

function MyReservationList() {
  const [reservations, setReservations] = useState([]);
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);

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
    setIsLoading(true);
    api.get("/reservation/my")
      .then(res => {
        setReservations(res.data.data || []);
      })
      .catch(err => {
        console.error("예약 조회 실패:", err);
        // 에러가 났을 때도 사용자에게 "내역 없음"을 보여주기 위해 빈 배열로 설정
        setReservations([]);
        alert("예약 목록을 불러오는데 실패했습니다."); // 사용자에게 에러 알림
      })
      .finally(() => {
        setIsLoading(false);
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
    const cancelReasonInput = window.prompt("취소 사유를 입력해주세요. (선택 사항):", "");

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

  return (
    <div className="mx-auto p-8 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">예약 내역</h2>
      <h2 className="text-xl font-bold mb-6">나의 예약 목록</h2>

      {/* 1. 아직 데이터를 불러오지 않았다면 (API 요청 중) 아무것도 렌더링하지 않음 */}
      {isLoading ? (
        null
      ) : reservations.length === 0 ? (
        // 예약 내역 없음 안내만 아래에!
        <div className="mt-12 text-gray-400 text-center">
          <p className="text-lg font-semibold">아직 예약 내역이 없어요.</p>
          <p className="mt-2">반려견과 함께하는 즐거운 스파 경험, 지금 바로 시작해보세요!</p>
          <SpurfyButton
            onClick={() => navigate('/spalist')}
            className="mt-6 px-4 py-2"
            variant="primary"
          >
            서비스 예약하러 가기
          </SpurfyButton>
        </div>
      ) : (
        reservations.map((r) => (
          <div
            key={r.reservationId}
            className="w-full border border-gray-200 p-4 mb-4 rounded-xl shadow-sm cursor-pointer hover:bg-sky-50 flex items-stretch gap-4"
            onClick={() => handleItemClick(r.reservationId)}
          >
            {/* ⭐ 1. 왼쪽: 이미지 영역 ⭐ */}
            <div className="w-24 h-24 bg-gray-200 rounded-lg flex-shrink-0 flex items-center justify-center overflow-hidden">
              <span className="text-gray-500 text-sm">이미지</span>
            </div>

            {/* ⭐ 2. 가운데: 예약 정보 텍스트 (서비스명, 날짜, 가격)⭐ */}
            <div className="flex-grow flex flex-col justify-between mt-1">
              <div> {/* 서비스명 */}
                <p className="text-lg font-bold text-gray-800">{r.serviceName}</p>
                {/* 날짜, 가격 */}
                <p className="text-gray-400 text-sm mb-4">예약날짜 : {r.reservationDate} {r.reservationTime}</p>
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
                    className="px-2 py-1 text-sm"
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
                    className="px-2 py-1 text-sm"
                  >
                    리뷰작성
                  </SpurfyButton>
                )}
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default MyReservationList;