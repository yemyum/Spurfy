import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';
import StarRating from '../components/Common/StarRating';

function SpaReviewsPage() {
    const { spaSlug } = useParams();
    const [reviews, setReviews] = useState([]);
    const [spaName, setSpaName] = useState('');
    const [averageRating, setAverageRating] = useState(0);
    const [totalReviews, setTotalReviews] = useState(0); // 총 리뷰 개수 추가

    const [page, setPage] = useState(0); // 현재 페이지 번호
    const [hasMore, setHasMore] = useState(true); // 더 가져올 리뷰가 있는지 여부

    // 리뷰 데이터를 가져오는 함수
    const fetchReviews = async () => {
        if (!hasMore) return; // 더 가져올 리뷰가 없으면 함수 종료

        try {
            // API 호출 시 page와 size를 전달
            const res = await api.get(`/reviews/public/slug/${spaSlug}?page=${page}&size=5`);
            const newReviews = res.data.data.content;
            const totalPages = res.data.data.totalPages;
            const totalElements = res.data.data.totalElements;

            setReviews(prevReviews => page === 0 ? newReviews : [...prevReviews, ...newReviews]);

            // ⭐ 총 리뷰 개수만 첫 페이지에서 설정 ⭐
            if (page === 0) {
                setTotalReviews(totalElements);
            }

            setHasMore(page < totalPages - 1);

        } catch (error) {
            console.error('리뷰 불러오기 실패:', error);
            setHasMore(false);
        }
    };

    useEffect(() => {
        if (reviews.length > 0) {
            // 전체 리뷰 배열의 총합을 구해서
            const totalRating = reviews.reduce((sum, r) => sum + r.rating, 0);
            // 전체 리뷰 개수로 나누기!
            setAverageRating(totalRating / reviews.length);
        } else {
            setAverageRating(0); // 리뷰가 없을 때는 0점
        }
    }, [reviews]); // reviews 배열이 바뀔 때마다 실행

    // 스파 상세 정보를 가져오는 useEffect (한 번만 실행)
    useEffect(() => {
        api.get(`/spa-services/slug/${spaSlug}`)
            .then(res => {
                setSpaName(res.data.data.name);
                // 스파 정보를 가져온 후 첫 페이지 리뷰를 불러오기
                fetchReviews();
            })
            .catch(() => console.error('스파 정보 불러오기 실패'));
    }, [spaSlug]);

    // 스크롤을 감지해서 다음 페이지를 부르는 useEffect
    useEffect(() => {
        const handleScroll = () => {
            const isAtBottom = window.innerHeight + document.documentElement.scrollTop >= document.documentElement.offsetHeight - 100;

            // ⭐ 스크롤이 바닥에 닿고, 더 불러올 리뷰가 있을 때만 페이지 번호를 증가!
            if (isAtBottom && hasMore) {
                setPage(prevPage => prevPage + 1);
            }
        };

        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, [hasMore]); // hasMore가 바뀔 때만 리스너를 업데이트

    return (
        <div className="w-full h-full p-8 bg-white select-none">
            <div className="max-w-4xl mx-auto">
                {reviews.length > 0 && (
                    <div className="flex flex-col items-center mt-8 mb-8">
                        <h3 className="text-2xl font-semibold mb-4">{spaName}</h3>

                        <div className="flex flex-col items-center gap-1">
                            <span className="font-semibold text-4xl">{averageRating.toFixed(1)}
                                <span className="text-gray-400 text-base"> / 5.0</span></span>
                            <StarRating rating={averageRating} readOnly={true} size="middle" />
                        </div>

                        <span className="text-gray-400 mt-4">총 {reviews.length} 개 후기</span>
                    </div>
                )}

                {reviews.length === 0 && <div className="text-center text-gray-400 text-lg font-semibold">아직 작성된 리뷰가 없습니다.</div>}

                <div className="flex items-center justify-center space-y-4">
                    {reviews.map(r => (
                        <div key={r.reviewId} className="w-full border-2 border-gray-200 rounded-lg shadow-sm p-4 bg-white">
                            {/* ⭐ 첫 번째 줄: 닉네임만 ⭐ */}
                            <div className="font-semibold text-lg mb-1">{r.userNickname}</div>

                            {/* ⭐ 두 번째 줄: 별점, 작성일! ⭐ */}
                            <div className="flex items-center gap-2 mb-4">
                                <StarRating rating={r.rating} readOnly={true} size="middle" />
                                <span className="text-gray-400">
                                    {r.createdAt?.slice(0, 10)}
                                </span>
                            </div>

                            {/* ⭐ 세 번째 줄: 리뷰 내용 ⭐ */}
                            <div className="text-gray-800">{r.content}</div>

                            {/* ⭐ 네 번째 줄: 리뷰 이미지 (있으면) ⭐ */}
                            {r.imageUrl && (
                                <img
                                    src={r.imageUrl}
                                    alt="Review Image"
                                    className="max-w-full h-auto rounded-md mb-2"
                                />
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default SpaReviewsPage;