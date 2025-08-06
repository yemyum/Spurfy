import { useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import api from "../api/axios";
import StarRating from '../components/Common/StarRating';
import SpurfyButton from "../components/Common/SpurfyButton";

function ReviewWrite() {
  const location = useLocation();
  const navigate = useNavigate();
  const [reservationData, setReservationData] = useState(null);

   useEffect(() => {
    const stateData = location.state;
    
    console.log("💬 ReviewWrite에 location.state로 들어온 데이터 확인:", stateData);

    if (!stateData || !stateData.reservationId) {
      alert("리뷰를 작성할 예약 정보가 부족합니다. 다시 시도해주세요.");
      navigate('/mypage/reservations'); // 예약 목록으로 돌려보내기
      return;
    }

    // API 호출 없이 바로 stateData를 reservationData에 저장
    setReservationData(stateData); 

  }, [location.state, navigate]); // location.search가 변경될 때마다 재실행

  const [form, setForm] = useState({
    rating: 5,
    content: "",
    imageUrl: "",
  });

  const formatDate = (str) => {
  if (!str) return "";
  const [yyyy, mm, dd] = str.split("-");
  return `${yyyy}.${mm}.${dd}`;
};

  const handleSubmit = async () => {
    if (form.content.length < 30) {
      alert("리뷰는 최소 30자 이상 작성해주세요!");
      return;
    }

    if (!reservationData) { // 예약 정보가 없으면 제출 못하게 막기
      alert("예약 정보가 로딩되지 않아 리뷰를 제출할 수 없습니다.");
      return;
    }

    try {
      await api.post("/reviews", {
        reservationId: reservationData.reservationId,
        dogId: reservationData.dogId,
        ...form
      });
      alert("리뷰가 등록되었습니다!");
      navigate("/mypage/reviews");
    } catch (err) {
      alert(err.response?.data?.message || "리뷰 등록 실패");
    }
  };

  const handleRatingChange = (newRating) => {
    setForm({ ...form, rating: newRating });
  };

  if (!reservationData) {
    return null; // 데이터 없으면 아무것도 렌더링하지 않음
  }

  return (
    <div className="max-w-2xl mx-auto mt-10 mb-10 p-6 select-none space-y-6 bg-white shadow-md rounded-xl border border-gray-200">
      <h2 className="text-2xl font-bold mb-4 text-spurfyBlue">리뷰 작성</h2>
      <p className="font-semibold text-gray-400">우리 아이의 소중한 경험이 다른 친구들에게 도움을 줄 수 있어요!</p>

      <div className="bg-gray-50 p-4 rounded-lg">
         <p className="text-xl font-semibold">{reservationData.serviceName}</p>
        <p>
           {reservationData.dogName} | {formatDate(reservationData.reservationDate)}
         </p>
      </div>
    
     <div className="pb-2 border-b border-gray-200">
      <label className="block text-lg font-semibold mb-2">별점을 선택해주세요.</label>
      {/* StarRating 컴포넌트 삽입 및 props 이름 맞추기! */}
      <div>
        <StarRating
          rating={form.rating}
          onRate={handleRatingChange}
          readOnly={false}
          size="large"
        />
      </div>
      </div>

     <div className="pb-2 border-b border-gray-200">
      <label className="block text-lg font-semibold mb-2">리뷰를 작성해주세요.</label>
      <textarea
        rows="4"
        className="border border-gray-200 p-3 rounded w-full mb-4 focus:outline-none"
        placeholder="최소 30자 이상 작성해주세요."
        value={form.content}
        onChange={(e) => setForm({ ...form, content: e.target.value })}
      ></textarea>
     </div>

     <div className="pb-2">
      <label className="block text-lg font-semibold mb-2">사진 첨부하기</label>
      <input
        type="text"
        placeholder="이미지 URL 입력"
        className="border p-2 rounded w-full"
        value={form.imageUrl}
        onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
      />

      {form.imageUrl && (
        <div className="mb-4">
          <img
            src={form.imageUrl}
            alt="리뷰 이미지 미리보기"
            className="w-32 h-32 object-cover border rounded"
          />
        </div>
      )}
      </div>

      <SpurfyButton variant="primary"
        onClick={handleSubmit}
        className="w-full py-2 font-semibold"
      >
        등록하기
      </SpurfyButton>
    </div>
  );
}

export default ReviewWrite;