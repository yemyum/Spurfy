import { useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import api from "../api/axios";
import StarRating from '../components/Common/StarRating';

function ReviewWrite() {
  const { state } = useLocation();
  const navigate = useNavigate();

   useEffect(() => {
    console.log("💬 ReviewWrite에 들어온 state 확인:", state);
  }, []);


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

    try {
      await api.post("/reviews", {
        reservationId: state.reservationId,
        dogId: state.dogId,
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

  return (
    <div className="max-w-2xl mx-auto mt-10 p-6 bg-white shadow-md rounded-lg">
      <h2 className="text-2xl font-bold text-blue-800 mb-4">리뷰 작성</h2>
      <p className="text-gray-600 mb-1">우리 아이의 소중한 경험이 다른 친구들에게도 도움이 될 수 있어요!</p>

      <div className="bg-gray-50 p-4 rounded-lg mt-4 mb-6">
        <p className="text-lg font-semibold">{state.serviceName}</p>
        <p className="text-sm text-gray-600">
           {state.dogName} / {formatDate(state.reservationDate)}
         </p>
      </div>

      <label className="block font-semibold mb-1">별점을 선택해주세요.</label>
      

      <label className="block font-semibold mb-1">리뷰를 작성해주세요.</label>
      {/* StarRating 컴포넌트 삽입 및 props 이름 맞추기! */}
      <div className="mb-4">
        <StarRating
          rating={form.rating}
          onRate={handleRatingChange}
          readOnly={false}
          size="large"
        />
      </div>
      <textarea
        rows="4"
        className="border p-3 rounded w-full mb-4"
        placeholder="최소 30자 이상 작성해주세요."
        value={form.content}
        onChange={(e) => setForm({ ...form, content: e.target.value })}
      ></textarea>

      <label className="block font-semibold mb-1">사진 첨부하기</label>
      <input
        type="text"
        placeholder="이미지 URL 입력"
        className="border p-2 rounded w-full mb-4"
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

      <button
        onClick={handleSubmit}
        className="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded font-bold"
      >
        등록하기
      </button>
    </div>
  );
}

export default ReviewWrite;