import React from 'react';
import { useRouteError, isRouteErrorResponse } from 'react-router-dom'; // 에러 정보를 가져올 때 사용

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
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '80vh', 
      textAlign: 'center',
      backgroundColor: '#f8f8f8',
      padding: '20px',
      borderRadius: '10px',
      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
    }}>
      <h1 style={{ fontSize: '4rem', color: '#ff69b4', marginBottom: '20px' }}>{errorStatus}</h1>
      <p style={{ fontSize: '1.5rem', color: '#333', marginBottom: '30px' }}>{errorMessage}</p>
      <button 
        onClick={() => window.location.href = '/'} // 홈으로 돌아가는 버튼
        style={{ 
          padding: '10px 20px', 
          backgroundColor: '#ff69b4', 
          color: 'white', 
          border: 'none', 
          borderRadius: '5px', 
          cursor: 'pointer', 
          fontSize: '1rem' 
        }}
      >
        홈으로 돌아가기
      </button>
    </div>
  );
}

export default NotFoundPage;