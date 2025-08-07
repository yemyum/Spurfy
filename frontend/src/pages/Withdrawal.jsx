// src/pages/Withdrawal.jsx
import React, { useState } from 'react';
import api from '../api/axios';
import { useNavigate } from 'react-router-dom';
import SpurfyButton from '../components/Common/SpurfyButton';

function Withdrawal() {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  const [password, setPassword] = useState(''); // 비밀번호 입력 상태
  const [reason, setReason] = useState(''); // 탈퇴 사유 입력 상태 (초기값 빈 문자열)
  const [agreeToTerms, setAgreeToTerms] = useState(false); // 약관 동의 체크박스 상태
  const [error, setError] = useState(null); // 에러 메시지 상태

  // 탈퇴 사유 드롭다운 옵션들
  const reasonsOptions = [ // 'reasons'가 아니라 'reasonsOptions'로 이름 변경해서 혼동 줄이기
    '서비스 이용이 불편합니다.',
    '더 이상 해당 서비스가 필요하지 않습니다.',
    '개인 정보 보호 및 보안 문제가 우려됩니다.',
    '다른 서비스를 이용하게 되었습니다.',
    '기타 (직접 입력)', // '기타' 옵션
  ];

  // 탈퇴하기 버튼 클릭 시 실행되는 함수
  const handleWithdrawalSubmit = async (e) => {
    e.preventDefault(); // 폼 제출 시 페이지 새로고침 방지

    // 1. 이용약관 동의 필수 확인
    if (!agreeToTerms) {
      setError("이용약관 동의가 필요합니다.");
      return; // 함수 종료
    }
    // 2. 비밀번호 필수 확인
    if (!password) {
      setError("비밀번호를 입력해주세요.");
      return; // 함수 종료
    }
    // 3. 탈퇴 사유는 이제 '선택 사항'이므로, 여기서 필수로 검사하지 않아.
    //    만약 '기타'를 선택했는데 입력 내용이 없다면 백엔드에서 빈 문자열로 보낼 거야.

    // 최종 확인 알림창
    if (!window.confirm("정말로 회원 탈퇴를 진행하시겠습니까? 복구할 수 없습니다.")) {
      return; // '취소' 누르면 함수 종료
    }

    try {
      // 백엔드 API 호출 (DELETE /api/mypage/withdrawal)
      const res = await api.delete('/mypage/withdrawal', {
        data: { // DELETE 요청 시 body에 데이터를 담으려면 'data' 필드 사용
          password,
          // '탈퇴 사유를 선택하거나 입력해주세요.'라는 기본 옵션이 선택되어 있다면 빈 문자열로 전송
          reason: reason === "탈퇴 사유를 선택하거나 입력해주세요." ? "" : reason,
          agreeToTerms
        }
      });

      if (res.data.code === 'S001') {
        alert('회원 탈퇴가 성공적으로 처리되었습니다. 이용해주셔서 감사합니다.');

        localStorage.removeItem('token'); // JWT 토큰 삭제
        localStorage.removeItem('refreshToken'); // 리프레시 토큰도 삭제!

        navigate('/login'); // 탈퇴 성공 시 로그인 페이지로 이동
      } else {
        // 백엔드에서 에러 메시지가 온 경우
        setError(res.data.message || '회원 탈퇴에 실패했습니다.');
      }
    } catch (err) {
      console.error('회원 탈퇴 실패:', err);
      // 네트워크 오류나 백엔드에서 예상치 못한 에러가 온 경우
      setError(err.response?.data?.message || '회원 탈퇴 중 네트워크 오류가 발생했습니다.');
    }
  };

  return (
    <div className="max-w-2xl mx-auto mt-10 mb-10 bg-white rounded-xl shadow-md border border-gray-200 p-10">
      <h2 className="text-2xl font-bold mb-12 mt-4 text-center">회원 탈퇴</h2>

      <form onSubmit={handleWithdrawalSubmit}> {/* 폼 제출 시 handleWithdrawalSubmit 실행 */}
        {/* 비밀번호 입력 필드 */}
        <div className="mb-4">
          <label htmlFor="password" className="block font-semibold mb-2">
            비밀번호 확인
          </label>
          <input
            type="password"
            id="password"
            className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required // 비밀번호는 여전히 필수
          />
        </div>

        {/* 탈퇴 사유 선택/입력 필드 */}
        <div className="mb-4">
          <label htmlFor="reason" className="block font-semibold mb-2">
            탈퇴 사유 (선택 사항)
          </label>
          <select
            id="reason"
            className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-100 mb-2"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          >
            <option value="">탈퇴 사유를 선택하거나 입력해주세요.</option> {/* 기본 선택 옵션 */}
            {reasonsOptions.map((r, index) => ( // 옵션들을 맵핑
              <option key={index} value={r}>{r}</option>
            ))}
          </select>
          {/* '기타 (직접 입력)' 옵션이 선택되면 텍스트 에리어 보여주기 */}
          {reason === "기타 (직접 입력)" && (
            <textarea
              className="appearance-none border border-gray-300 rounded w-full py-2 px-3 leading-tight focus:outline-none focus:ring-2 focus:ring-gray-100"
              placeholder="기타 사유를 입력해주세요."
              value={reason.startsWith("기타: ") ? reason.substring(6) : ""} // '기타: ' 접두사 제거하고 보여주기
              onChange={(e) => setReason("기타: " + e.target.value)} // '기타: ' 접두사 붙여서 상태에 저장
            />
          )}
        </div>
        <div className="border border-gray-200 bg-gray-100 p-4 rounded-md mb-4 max-h-48 overflow-y-auto">
          <p className="font-semibold text-gray-700 mb-2">회원 탈퇴 약관 및 유의사항</p>
            <ul className="list-disc list-inside text-sm text-gray-500">
              <li>탈퇴 시, 회원님의 개인정보 및 이용 기록은 즉시 삭제됩니다.</li>
              <li>예약 내역, 결제 정보 등 모든 이용 기록이 사라집니다.</li>
              <li>작성하신 게시물은 삭제되지 않으며, 탈퇴 전 직접 삭제하셔야 합니다.</li>
              <li>탈퇴한 계정은 재가입 시에도 복구되지 않습니다.</li>
            </ul>
        </div>

        {/* 이용 약관 동의 체크박스 */}
        <div className="mb-6 flex items-center">
          <input
            type="checkbox"
            id="agreeToTerms"
            className="accent-[#3B82F6] w-4 h-4 mr-2 leading-tight"
            checked={agreeToTerms}
            onChange={(e) => setAgreeToTerms(e.target.checked)}
            required // 약관 동의는 필수
          />
          <label htmlFor="agreeToTerms" className="text-base text-gray-700">
            회원 탈퇴 약관 및 유의사항을 모두 확인하였으며 동의합니다.
            {/* 여기에 실제 약관 내용을 링크하거나 모달로 띄울 수 있어 */}
          </label>
        </div>

        {/* 에러 메시지 표시 */}
        {error && (
          <p className="text-red-500 text-sm mb-4 text-center">{error}</p>
        )}

        {/* 버튼들: 취소 및 탈퇴하기 */}
        <div className="flex justify-between pt-4">
          <button
            type="button" // 폼 제출이 아닌 일반 버튼
            onClick={() => navigate(-1)} // 이전 페이지로 돌아가기
            className="px-4 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg shadow-sm hover:bg-gray-300 transition duration-300"
          >
            취소
          </button>
          <SpurfyButton variant='danger'
            type="submit" // 폼 제출 버튼
            className="px-4 py-2 shadow-sm"
          >
            탈퇴하기
          </SpurfyButton>
        </div>
      </form>
    </div>
  );
}

export default Withdrawal;