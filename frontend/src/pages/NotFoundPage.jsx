import React from 'react';
import { useRouteError, isRouteErrorResponse } from 'react-router-dom'; // 에러 정보를 가져올 때 사용
import SpurfyButton from '../components/Common/SpurfyButton';

function NotFoundPage() {
  const error = useRouteError(); // 라우터 에러 정보를 가져옴
  console.error(error); // 개발자 도구에서 에러 확인용

  let errorMessage = "페이지를 찾을 수 없어요. 주소를 다시 확인해주세요!"; // 기본값을 404 메시지로 변경
  let errorStatus = 404; // 기본 상태 코드도 404로 변경 (가장 흔한 NotFound 상황)

  // ✨ error 객체가 존재할 때만 isRouteErrorResponse를 확인하도록 수정
  if (error) { 
    if (isRouteErrorResponse(error)) {
      errorStatus = error.status;
      if (error.status === 404) {
        errorMessage = "페이지를 찾을 수 없어요. 주소를 다시 확인해주세요!";
      } else if (error.status === 401) {
        errorMessage = "접근 권한이 없어요. 로그인 후 다시 시도해주세요!";
      } else {
        // 500 같은 다른 라우터 에러의 경우
        errorMessage = `에러 발생 (${error.status}): ${error.statusText || error.data?.message || '알 수 없는 오류'}`;
      }
    } else {
      // isRouteErrorResponse가 아니지만 error 객체가 존재하는 경우 (일반 JS 에러 등)
      errorMessage = `예상치 못한 오류가 발생했어요: ${error.message || '알 수 없는 오류'}`;
      errorStatus = 500; // 이 경우는 500으로 간주
    }
  } 
  // 만약 error 자체가 null이면, 위 기본값 (404 Not Found)이 그대로 사용됨.


  return (
    <div className='bg-[#F1FAFF] min-h-screen'>
      <div className="min-h-screen flex flex-col justify-center">
    <div className="flex flex-col items-center border border-gray-200 rounded-xl shadow-md bg-white/60 mx-auto -mt-40 p-28">
      <h1 className="text-[#67F3EC] font-logo text-[60px] mb-8">{errorStatus}</h1>
      <p className="font-semibold text-gray-500 text-2xl mb-12">{errorMessage}</p>
      <SpurfyButton variant='ai' 
        onClick={() => window.location.href = '/'} // 홈으로 돌아가는 버튼
        className='px-6 py-2 text-xl shadow-sm'
      >
        홈으로 돌아가기
      </SpurfyButton>
    </div>
  </div>
</div>
  );
}

export default NotFoundPage;