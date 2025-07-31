import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import SpurfyButton from "../components/Common/SpurfyButton";

function MyReviewList() {
  const [reviews, setReviews] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    api.get("/reviews/my")
      .then((res) => setReviews(res.data.data))
      .catch((err) => console.error("리뷰 목록 조회 실패:", err));
  }, []);

  return (
    <div className="mx-auto p-8 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">리뷰 조회</h2>
      <h2 className="text-xl font-bold mb-6">내가 작성한 리뷰 리스트</h2>
      {reviews.map((r) => (
        <div
          key={r.reviewId}
          className="border border-gray-200 p-4 mb-4 rounded-md shadow-sm cursor-pointer hover:bg-blue-50"
          onClick={() => navigate(`/mypage/reviews/${r.reviewId}`, { state: r })}
        >
          <h3
            className="font-semibold text-lg hover:underline inline-block"
            onClick={(e) => {
              e.stopPropagation();
              if (r.spaSlug) {
                navigate(`/spalist/slug/${r.spaSlug}`); 
              } else {
                // 슬러그가 없으면 기존 ID로라도 이동시키거나, 에러 메시지 표시
                navigate(`/spalist/${r.serviceId}`); // 임시로 기존 ID 사용하거나,
                // alert('스파 정보를 찾을 수 없습니다.'); // 사용자에게 알림
              }
            }}
          >
            {r.serviceName}
          </h3>

          <p className="text-sm text-gray-400">작성일: {r.createdAt}</p>
          <p className="mt-2 text-gray-800 truncate">{r.content}</p>
          
          <div className="flex gap-2 mt-4">
            <SpurfyButton variant="primary"
            className="px-4 py-1 text-sm"
            onClick={(e) => {
            e.stopPropagation();
            navigate(`/mypage/reviews/${r.reviewId}`, { state: { ...r, isEditing: true } }); 
          }}
          >
          수정
          </SpurfyButton>
            <SpurfyButton variant="danger"
              className="px-4 py-1 text-sm"
              onClick={async (e) => {
                e.stopPropagation();
                if (!window.confirm("정말 이 리뷰를 삭제하시겠습니까?")) return;
                try {
                  await api.delete(`/mypage/reviews/${r.reviewId}`);
                  setReviews((prev) => prev.filter((item) => item.reviewId !== r.reviewId));
                } catch (err) {
                  alert("리뷰 삭제 실패");
                }
              }}
            >
              삭제
            </SpurfyButton>
          </div>
        </div>
      ))}
    </div>
  );
}

export default MyReviewList;