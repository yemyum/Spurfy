import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';
import StarRating from '../components/Common/StarRating';

function SpaReviewsPage() {
    const { spaSlug } = useParams();
    const [reviews, setReviews] = useState([]);
    const [spaName, setSpaName] = useState('');
    const [averageRating, setAverageRating] = useState(0);

    useEffect(() => {
        // 1. 스파 서비스 정보 가져오기 (리뷰 페이지 상단에 스파 이름 보여주기 위해)
        api.get(`/spa-services/slug/${spaSlug}`)
            .then(res => {
                setSpaName(res.data.data.name);
            })
            .catch(() => console.error('스파 정보 불러오기 실패'));

        // 2. 이 스파 서비스의 모든 리뷰 가져오기!
        api.get(`/reviews/public/slug/${spaSlug}`)
            .then(res => {
                const fetchedReviews = res.data.data || [];
                setReviews(fetchedReviews);
                // ⭐ 평균 별점 계산 ⭐
                if (fetchedReviews.length > 0) {
                    const totalRating = fetchedReviews.reduce((sum, r) => sum + r.rating, 0);
                    setAverageRating(totalRating / fetchedReviews.length);
                } else {
                    setAverageRating(0);
                }
            })
            .catch(error => {
                console.error('리뷰 불러오기 실패:', error);
                alert('리뷰를 불러오는 데 실패했어요! 😭');
            });
    }, [spaSlug]);

    if (!spaName) return <div>리뷰 로딩중...</div>; // 스파 이름이 없으면 로딩 중으로!

    return (
        <div className="container mx-auto p-4">
            <h2 className="text-2xl font-bold mb-4">{spaName} 의 모든 리뷰</h2>
            {reviews.length > 0 && (
                <div className="flex items-center mb-6 border-b pb-4">
                    <span className="font-semibold text-xl mr-2">총 평점: {averageRating.toFixed(1)}</span>
                    <StarRating rating={averageRating} readOnly={true} size="large" /> {/* 여기는 좀 더 크게! */}
                </div>
            )}
            {reviews.length === 0 && <div className="text-center text-gray-600">아직 작성된 리뷰가 없습니다!</div>}
            
            <div className="space-y-4"> {/* 리뷰들 사이에 간격 주기 */}
                {reviews.map(r => (
                    <div key={r.reviewId} className="border rounded-lg shadow-sm p-4 bg-white">
                        <div className="flex items-center justify-between mb-2">
                            <div className="font-semibold text-lg">{r.userNickname}</div>
                            <div>
                                <StarRating rating={r.rating} readOnly={true} size="medium" /> 
                            </div>
                        </div>
                        <div className="text-gray-800 mb-2">{r.content}</div>
                        {r.imageUrl && ( // 이미지가 있으면 보여주기
                            <img src={r.imageUrl} alt="Review Image" className="max-w-full h-auto rounded-md mb-2" />
                        )}
                        <div className="text-sm text-gray-500">
                            {r.dogName && <span className="mr-2">({r.dogName}과 함께)</span>}
                            {r.createdAt?.slice(0, 10)}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default SpaReviewsPage;