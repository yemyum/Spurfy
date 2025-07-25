import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div className="p-8 max-w-xl mx-auto text-center">
      <h1 className="text-3xl font-bold text-green-600 mb-4">스퍼피에 오신 걸 환영해요! 🐶🐾</h1>
      <p className="text-gray-700 text-base mb-6">
        AI가 사진을 분석해 <strong>우리 아이에게 딱 맞는 스파</strong>를 추천해드려요!
      </p>

      <Link
        to="/dog-spa-ai"
        className="inline-block px-6 py-3 bg-green-500 text-white text-lg font-semibold rounded-xl shadow-md hover:bg-green-600 transition"
      >
        🛁 스파 추천받으러 가기
      </Link>
    </div>
  );
}
