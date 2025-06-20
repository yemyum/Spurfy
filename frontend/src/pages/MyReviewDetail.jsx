// 📁 src/pages/MyReviewDetail.jsx
import { useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";

function MyReviewDetail() {
  const { state } = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (!state) {
      alert("잘못된 접근입니다.");
      navigate("/mypage/reviews");
    }
  }, [state]);

  if (!state) return null;

  return (
    <div className="review-detail">
      <h2 className="text-xl font-bold">{state.serviceName}</h2>
      <p className="text-sm text-gray-500">가격: {state.price?.toLocaleString()}원</p>
      <p className="mt-1">작성일: {state.createdAt}</p>
      <p className="mt-1">강아지 이름: {state.dogName}</p>
      <div className="my-2">⭐ {state.rating}</div>
      <img src={state.imageUrl} alt="리뷰 이미지" className="w-48 h-auto rounded" />
      <p className="mt-4 whitespace-pre-line">{state.content}</p>

      <div className="flex justify-end mt-4 gap-2">
        <button className="bg-red-200 px-4 py-1 rounded">삭제하기</button>
        <button className="bg-blue-200 px-4 py-1 rounded">수정하기</button>
      </div>
    </div>
  );
}

export default MyReviewDetail;