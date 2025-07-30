import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import StarRating from '../components/Common/StarRating';
import SpurfyButton from '../components/Common/SpurfyButton';
import api from '../api/axios';

function MyReviewDetail() {
  const { reviewId } = useParams();
  const navigate = useNavigate();
  const location = useLocation(); // ⭐ useLocation 훅 사용! ⭐

  const [reviewDetail, setReviewDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // ⭐⭐ isEditing 초기값을 location.state에서 가져오도록 수정! ⭐⭐
  // `state`가 없거나 `isEditing`이 없으면 기본값 `false`
  const [isEditing, setIsEditing] = useState(location.state?.isEditing || false); 

  const [editedRating, setEditedRating] = useState(0);
  const [editedContent, setEditedContent] = useState('');
  const [editedImageUrl, setEditedImageUrl] = useState('');

  useEffect(() => {
    if (reviewId) {
      const fetchReviewDetail = async () => {
        try {
          setLoading(true);
          setError(null);
          const response = await api.get(`/reviews/${reviewId}`);
          if (response.data.code === 'S001') {
            const data = response.data.data;
            setReviewDetail(data);
            setEditedRating(data.rating);
            setEditedContent(data.content);
            setEditedImageUrl(data.imageUrl || '');
          } else {
            setError(response.data.message || '리뷰 상세 정보를 불러오는데 실패했습니다.');
          }
        } catch (err) {
          setError(err.response?.data?.message || '네트워크 오류가 발생했습니다.');
        } finally {
          setLoading(false);
        }
      };
      fetchReviewDetail();
    }
  }, [reviewId]);

  const handleDelete = async () => {
    if (window.confirm("정말 이 리뷰를 삭제하시겠습니까?")) {
      try {
        await api.delete(`/reviews/${reviewId}`);
        alert("리뷰가 성공적으로 삭제되었습니다!");
        navigate('/mypage/reviews');
      } catch (err) {
        alert(err.response?.data?.message || "리뷰 삭제에 실패했습니다.");
      }
    }
  };

  const handleEditMode = () => {
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    if (reviewDetail) {
      setEditedRating(reviewDetail.rating);
      setEditedContent(reviewDetail.content);
      setEditedImageUrl(reviewDetail.imageUrl || '');
    }
    setIsEditing(false);
  };

  const handleUpdateSubmit = async () => {
    if (!window.confirm("리뷰를 수정하시겠습니까?")) return;
    try {
      const updateData = {
        rating: editedRating,
        content: editedContent,
        imageUrl: editedImageUrl,
      };
      const response = await api.put(`/reviews/${reviewId}`, updateData);
      if (response.data.code === 'S001') {
        alert("리뷰가 성공적으로 수정되었습니다!");
        setReviewDetail(prev => ({
          ...prev,
          rating: editedRating,
          content: editedContent,
          imageUrl: editedImageUrl,
          updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
        }));
        setIsEditing(false);
      } else {
        alert(response.data.message || "리뷰 수정에 실패했습니다.");
      }
    } catch (err) {
      alert(err.response?.data?.message || "리뷰 수정에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="p-5 max-w-2xl mx-auto">
        <div className="border rounded p-6 shadow-md bg-white">로딩 중...</div>
      </div>
    );
  }
  if (error) {
    return (
      <div className="p-5 max-w-2xl mx-auto">
        <div className="border rounded p-6 shadow-md bg-white text-red-600">에러 발생: {error}</div>
      </div>
    );
  }
  if (!reviewDetail) {
    return (
      <div className="p-5 max-w-2xl mx-auto">
        <div className="border rounded p-6 shadow-md bg-white">리뷰 정보를 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div className="mx-auto p-8 mb-6 select-none">
      <div className="text-2xl font-bold mb-6 text-spurfyBlue">리뷰 관리</div>

      <div className="border border-gray-200 py-6 rounded-md shadow-sm bg-white mb-6">
        <div className="pb-4 mb-4 border-b border-gray-200 px-6">
        <h3 className='text-xl font-semibold'>{reviewDetail.serviceName}</h3>
        <p className='text-lg font-semibold'>{reviewDetail.price}원</p>
        <p className='text-gray-400'>{reviewDetail.dogName} | 작성일 {reviewDetail.createdAt}</p>
        </div>

        {/* ⭐⭐ MUI StarRating 컴포넌트 사용 ⭐⭐ */}
        <div className="pb-4 mb-4 border-b border-gray-200 px-6">
          {isEditing ? (
            // 수정 모드일 때: onRate를 통해 setEditedRating 연결, readOnly={false}
            <StarRating rating={editedRating} onRate={setEditedRating} readOnly={false} /> 
          ) : (
            // 조회 모드일 때: readOnly={true}
            <StarRating rating={reviewDetail.rating} readOnly={true} />
          )}

        {/* 리뷰 내용 */}
        {isEditing ? (
          <textarea
            className="w-full p-2 border rounded mt-2 min-h-24 resize-y focus:outline-none"
            value={editedContent}
            onChange={(e) => setEditedContent(e.target.value)}
            rows="5"
            placeholder="리뷰 내용을 입력해주세요."
          />
        ) : (
          <p className="mt-4 leading-relaxed whitespace-pre-wrap">{reviewDetail.content}</p>
        )}

        {/* 이미지 URL 입력 */}
        {isEditing && (
          <div className="mt-4">
            <input
              type="text"
              className="w-full p-2 border rounded"
              value={editedImageUrl}
              onChange={(e) => setEditedImageUrl(e.target.value)}
              placeholder="이미지 URL을 입력하세요 (선택 사항)"
            />
          </div>
        )}
        </div>

        {/* 리뷰 이미지 */}
        {reviewDetail.imageUrl && !isEditing && (
          <div className="mt-5 text-center">
            <img src={reviewDetail.imageUrl} alt="Review" className="max-w-full h-auto rounded border" />
          </div>
        )}
        {/* 수정 중 이미지 미리보기 */}
        {isEditing && editedImageUrl && (
          <div className="mt-5 text-center">
            <p>미리보기:</p>
            <img src={editedImageUrl} alt="Review Preview" className="max-w-full h-auto rounded border" />
          </div>
        )}

        {reviewDetail.isBlinded && (
          <p className="text-red-500 font-bold mt-4 text-center">
            이 리뷰는 관리자에 의해 블라인드 처리되었습니다.
          </p>
        )}
        {reviewDetail.updatedAt && reviewDetail.updatedAt !== reviewDetail.createdAt && (
          <p className="text-sm text-gray-500 mt-2 text-right">
            (최종 수정일: {reviewDetail.updatedAt})
          </p>
        )}

        {/* 버튼 */}
        <div className="mt-6 flex justify-end gap-2 px-6">
          {isEditing ? (
            <>
              <SpurfyButton variant = "primary" onClick={handleUpdateSubmit} className="px-4 py-2 font-semibold transition duration-200">저장하기</SpurfyButton>
              <button onClick={handleCancelEdit} className="px-4 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg shadow-sm hover:bg-gray-300 transition duration-300">취소</button>
            </>
          ) : (
            <>
              <SpurfyButton variant = "danger" onClick={handleDelete} className="px-4 py-2 text-sm font-semibold transition duration-200">삭제하기</SpurfyButton>
              <SpurfyButton variant = "ai" onClick={handleEditMode} className="px-4 py-2 text-sm font-semibold transition duration-200">수정하기</SpurfyButton>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default MyReviewDetail;