import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
// import defaultProfile from '../assets/default-profile.png'; // 기본 프로필 이미지가 있다면 사용
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'; // ⭐ 아이콘 사용을 위해 추가
import { faCamera } from '@fortawesome/free-solid-svg-icons'; // ⭐ 카메라 아이콘 추가

function Profile() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null); // 현재 조회된 사용자 프로필
  const [loading, setLoading] = useState(true); // 로딩 상태

  // 수정 모드 관련 상태
  const [isEditing, setIsEditing] = useState(false); // 수정 모드 여부
  const [editedNickname, setEditedNickname] = useState(''); // 수정 중인 닉네임
  const [editedName, setEditedName] = useState(''); // 수정 중인 이름
  const [editedPhone, setEditedPhone] = useState(''); // 수정 중인 전화번호
  // const [profileImageFile, setProfileImageFile] = useState(null); // ⭐ 이미지 파일 상태 (추후 업로드 시 사용)
  // const [previewImageUrl, setPreviewImageUrl] = useState(''); // ⭐ 이미지 미리보기 URL (추후 업로드 시 사용)

  // ⭐⭐⭐ 닉네임 중복 확인 관련 상태 추가 ⭐⭐⭐
  const [nicknameCheckMessage, setNicknameCheckMessage] = useState(''); // 중복 확인 결과 메시지 (예: "사용 가능", "중복")
  const [isNicknameAvailable, setIsNicknameAvailable] = useState(false); // 닉네임 사용 가능한지 여부 (boolean)
  const [isNicknameChecked, setIsNicknameChecked] = useState(false); // '중복 확인' 버튼을 눌렀는지 여부

  // 비밀번호 변경 관련 상태
  const [currentPassword, setCurrentPassword] = useState(''); // 현재 비밀번호
  const [newPassword, setNewPassword] = useState(''); // 새로운 비밀번호
  const [confirmNewPassword, setConfirmNewPassword] = useState(''); // 새로운 비밀번호 확인
  const [passwordChangeError, setPasswordChangeError] = useState(null); // 비밀번호 변경 에러 메시지

  // ⭐⭐ 1. 프로필 정보 조회 (GET /api/mypage/profile) ⭐⭐
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const res = await api.get('/mypage/profile');
        if (res.data.code === 'S001') {
          const fetchedProfile = res.data.data;
          setProfile(fetchedProfile);
          // 수정 모드 진입을 대비하여 초기값 설정
          setEditedNickname(fetchedProfile.nickname);
          setEditedName(fetchedProfile.name);
          setEditedPhone(fetchedProfile.phone);
          // if (fetchedProfile.profileImageUrl) { // ⭐ 프로필 이미지 URL이 있다면
          //   setPreviewImageUrl(fetchedProfile.profileImageUrl);
          // } else {
          //   setPreviewImageUrl(defaultProfile); // 기본 이미지 설정
          // }
          setIsNicknameAvailable(true);
          setIsNicknameChecked(true);
          setNicknameCheckMessage('현재 사용 중인 닉네임입니다.'); // 초기 메시지
        } else {
          alert(res.data.message || '프로필 정보를 불러오는데 실패했습니다.');
        }
      } catch (err) {
        console.error('프로필 조회 실패:', err);
        alert(err.response?.data?.message || '프로필을 불러오지 못했어요 😢');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  // ⭐⭐⭐ 회원 탈퇴 버튼 클릭 시 페이지 이동 함수 ⭐⭐⭐
  const handleGoToWithdrawal = () => {
    // /mypage/withdrawal 페이지로 이동
    navigate('/mypage/withdrawal');
  };

  const handleCheckNickname = async () => {
    if (editedNickname.trim() === '') {
      setNicknameCheckMessage('닉네임을 입력해주세요.');
      setIsNicknameAvailable(false);
      setIsNicknameChecked(false);
      return;
    }

    // ⭐ 현재 닉네임이 기존 닉네임과 동일하면 중복 확인 불필요 ⭐
    // 이 부분은 서비스 로직에서도 했지만, 프론트에서 먼저 막으면 API 호출 낭비를 줄여줘.
    if (profile && editedNickname === profile.nickname) {
        setNicknameCheckMessage('현재 사용 중인 닉네임입니다.');
        setIsNicknameAvailable(true); // 자기 닉네임이므로 사용 가능으로 간주
        setIsNicknameChecked(true);
        return;
    }

    try {
      // 백엔드 API 호출: /api/mypage/check-nickname?nickname={editedNickname}
      const res = await api.get(`/mypage/check-nickname?nickname=${editedNickname}`);
      if (res.data.code === 'S001') {
        const available = res.data.data; // true 또는 false
        setIsNicknameAvailable(available);
        setNicknameCheckMessage(res.data.message); // "사용 가능한 닉네임입니다." 또는 "이미 사용 중인 닉네임입니다."
        setIsNicknameChecked(true); // 중복 확인 완료!
      } else {
        // 서버에서 에러가 났지만 API 응답 형식에 맞게 왔다면 (예: 닉네임 유효성 검사 실패 등)
        setNicknameCheckMessage(res.data.message || '닉네임 확인 중 오류가 발생했습니다.');
        setIsNicknameAvailable(false);
        setIsNicknameChecked(false);
      }
    } catch (err) {
      console.error('닉네임 중복 확인 실패:', err);
      // 백엔드에서 409 CONFLICT 등으로 에러를 보낸 경우
      const errorMessage = err.response?.data?.message || '닉네임 확인 중 네트워크 오류가 발생했습니다.';
      setNicknameCheckMessage(errorMessage);
      setIsNicknameAvailable(false);
      setIsNicknameChecked(false);
    }
  };

  // ⭐⭐ 2. 프로필 정보 수정 (PATCH/PUT /api/mypage/profile) ⭐⭐
  const handleUpdateProfile = async () => {
    // ⭐⭐⭐ 프로필 수정 시 닉네임 중복 확인 여부 검사 추가 ⭐⭐⭐
    // 닉네임이 변경되었는데 (기존 닉네임과 다를 때),
    // 1. 중복 확인 버튼을 누르지 않았으면 경고
    if (isEditing && editedNickname !== profile.nickname && !isNicknameChecked) {
      alert("변경하려는 닉네임에 대해 중복 확인을 해주세요.");
      return;
    }
    // 2. 중복 확인은 했지만 사용 불가능한 닉네임이라면 경고
    if (isEditing && editedNickname !== profile.nickname && !isNicknameAvailable) {
        alert("사용할 수 없는 닉네임입니다. 다시 확인해주세요.");
        return;
    }

    if (!window.confirm("프로필 정보를 수정하시겠습니까?")) return;

    try {
      // 백엔드로 보낼 데이터 (닉네임, 이름, 전화번호)
      const updateData = {
        nickname: editedNickname,
        name: editedName,
        phone: editedPhone,
      };

      // 백엔드 프로필 수정 API 호출 (PUT /api/mypage/profile)
      const res = await api.put('/mypage/profile', updateData);

      if (res.data.code === 'S001') {
        alert('프로필이 성공적으로 수정되었습니다!');
        setProfile(res.data.data); // 서버에서 받은 최신 정보로 프로필 상태 업데이트
        setIsEditing(false); // 수정 모드 종료
        // ⭐ 수정 성공 후 닉네임 관련 상태 다시 초기화/설정 ⭐
        setIsNicknameAvailable(true); // 이제 저장된 닉네임은 자기 닉네임이므로 사용 가능
        setIsNicknameChecked(true); // 확인된 것으로 간주
        setNicknameCheckMessage('변경사항이 저장되었습니다.'); // 메시지 업데이트
      } else {
        alert(res.data.message || '프로필 수정에 실패했습니다.');
      }
    } catch (err) {
      console.error("프로필 수정 실패:", err);
      // 백엔드에서 닉네임 중복 에러가 왔을 경우 처리 (예: CustomException의 DUPLICATE_NICKNAME)
      const errorMessage = err.response?.data?.message || '프로필 수정 중 네트워크 오류가 발생했습니다.';
      alert(errorMessage); // 사용자에게 에러 메시지 보여줌
      // 만약 닉네임 중복 에러였다면, 해당 메시지를 닉네임 필드 아래에 보여주는 것도 좋아.
      if (err.response?.data?.code === 'U002') { // DUPLICATE_NICKNAME의 에러 코드 (U002)
          setNicknameCheckMessage(errorMessage);
          setIsNicknameAvailable(false);
          setIsNicknameChecked(true); // 중복 확인은 된 것이므로 true로 설정
      }
    }
  };

  // ⭐⭐ 3. 비밀번호 변경 (PUT /api/mypage/password) ⭐⭐
  const handleChangePassword = async () => {
    // 유효성 검사
    if (!currentPassword || !newPassword || !confirmNewPassword) {
      setPasswordChangeError("모든 비밀번호 필드를 입력해주세요.");
      return;
    }
    if (newPassword !== confirmNewPassword) {
      setPasswordChangeError("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    if (newPassword.length < 8 || newPassword.length > 16) {
      setPasswordChangeError("새 비밀번호는 8~16자 이내여야 합니다.");
      return;
    }

    if (!window.confirm("비밀번호를 변경하시겠습니까?")) return;

    try {
      const res = await api.put('/mypage/password', { // ⭐ 백엔드 API 경로 확인! ⭐
        currentPassword,
        newPassword,
      });

      if (res.data.code === 'S001') {
        alert('비밀번호가 성공적으로 변경되었습니다!');
        // 성공 시 필드 초기화
        setCurrentPassword('');
        setNewPassword('');
        setConfirmNewPassword('');
        setPasswordChangeError(null); // 에러 메시지 초기화
      } else {
        setPasswordChangeError(res.data.message || '비밀번호 변경에 실패했습니다.');
      }
    } catch (err) {
      console.error("비밀번호 변경 실패:", err);
      setPasswordChangeError(err.response?.data?.message || '네트워크 오류가 발생했습니다.');
    }
  };

  // ⭐⭐⭐ 닉네임 변경 시 중복 확인 상태 초기화 ⭐⭐⭐
  const handleNicknameChange = (e) => {
    setEditedNickname(e.target.value);
    // 닉네임 변경 시 중복 확인 상태 초기화
    setIsNicknameAvailable(false);
    setNicknameCheckMessage('');
    setIsNicknameChecked(false); // 다시 중복 확인 필요
  };

  // ⭐ 로딩 및 데이터 없음 처리 ⭐
  if (loading) {
    return (
      <div className="p-5 max-w-2xl mx-auto">
        <div className="border rounded p-6 shadow-md bg-white">로딩 중...</div>
      </div>
    );
  }
  if (!profile) {
    return (
      <div className="p-5 max-w-2xl mx-auto">
        <div className="border rounded p-6 shadow-md bg-white text-red-600">
          프로필 정보를 불러올 수 없습니다. 다시 시도해주세요.
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto bg-white p-8 rounded shadow">
      <h2 className="text-2xl font-bold mb-6 text-blue-500">내 프로필</h2>

      {/* 프로필 이미지 */}
      <div className="flex flex-col items-center justify-center mb-6 relative">
        <div className="w-32 h-32 rounded-full border-2 border-gray-300 flex items-center justify-center overflow-hidden bg-gray-100">
          {/* ⭐ 실제 프로필 이미지가 있다면 여기에 img 태그 사용 ⭐ */}
          {/* profile.profileImageUrl 또는 previewImageUrl 사용 */}
          {/* 예시: <img src={previewImageUrl || defaultProfile} alt="프로필 이미지" className="w-full h-full object-cover" /> */}
        </div>
        <button className="mt-2 text-sm text-blue-500 hover:underline">
          <FontAwesomeIcon icon={faCamera} />
          사진 편집하기
        </button>
        {/* ⭐ 이미지 파일 선택 input (hidden으로 만들고 버튼 클릭 시 트리거) ⭐ */}
        {/* <input type="file" accept="image/*" className="hidden" onChange={handleImageChange} /> */}
      </div>

      {/* 정보 리스트 또는 수정 폼 */}
      <div className="space-y-4">
        <ProfileInfoRow label="이름" value={profile.name} isEditing={isEditing} editedValue={editedName} onEditChange={setEditedName} type="text" />
        <ProfileInfoRow label="닉네임" value={profile.nickname} isEditing={isEditing} editedValue={editedNickname} onEditChange={setEditedNickname} type="text" />
        
        {/* ⭐⭐⭐ 닉네임 필드: ProfileInfoRow 대신 직접 여기에 구현 ⭐⭐⭐ */}
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">닉네임</label>
          {isEditing ? ( // 수정 모드일 때
            <div className="flex items-center gap-2"> {/* flex로 input과 버튼을 한 줄에 배치 */}
              <input
                type="text"
                className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                value={editedNickname}
                onChange={handleNicknameChange} // 위에서 만든 핸들러 연결
                placeholder="새 닉네임을 입력하세요."
              />
              <button
                onClick={handleCheckNickname} // 위에서 만든 중복 확인 핸들러 연결
                className="whitespace-nowrap px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition duration-300 text-sm"
              >
                중복 확인
              </button>
            </div>
          ) : ( // 조회 모드일 때
            <p className="text-gray-900 text-lg">{profile.nickname}</p>
          )}
          {/* 닉네임 중복 확인 메시지 표시 */}
          {isEditing && nicknameCheckMessage && ( // 수정 모드이고 메시지가 있을 때만 표시
            <p className={`text-sm mt-1 ${isNicknameAvailable ? 'text-green-600' : 'text-red-600'}`}>
              {nicknameCheckMessage}
            </p>
          )}
        </div>
        {/* ⭐⭐⭐ 닉네임 필드 끝 ⭐⭐⭐ */}

        <ProfileInfoRow label="전화번호" value={profile.phone} isEditing={isEditing} editedValue={editedPhone} onEditChange={setEditedPhone} type="tel" />
        <ProfileInfoRow label="이메일" value={profile.email} isEditing={false} type="email" /> {/* 이메일은 수정 불가 */}
      </div>

      {/* 프로필 수정/저장/취소 버튼 */}
      <div className="mt-8 flex justify-end gap-3">
        {isEditing ? (
          <>
            <button
              onClick={handleUpdateProfile}
              className="px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition duration-300"
            >
              저장
            </button>
            <button
              onClick={() => {
                setIsEditing(false);
                // 수정 취소 시 원래 값으로 되돌리기
                setEditedNickname(profile.nickname);
                setEditedName(profile.name);
                setEditedPhone(profile.phone);
                // setProfileImageFile(null); // ⭐ 이미지 파일 초기화
                // setPreviewImageUrl(profile.profileImageUrl || defaultProfile); // ⭐ 이미지 미리보기 초기화
              }}
              className="px-6 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 transition duration-300"
            >
              취소
            </button>
          </>
        ) : (
          <button
            onClick={() => setIsEditing(true)}
            className="px-6 py-2 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition duration-300"
          >
            프로필 수정
          </button>
        )}
      </div>

      {/* 비밀번호 변경 섹션 */}
      <div className="mt-10 pt-8 border-t border-gray-200">
        <h3 className="text-xl font-bold mb-5 text-gray-700">비밀번호 변경</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-gray-700 text-sm font-semibold mb-2">현재 비밀번호</label>
            <input
              type="password"
              className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-300"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="현재 비밀번호를 입력해주세요."
            />
          </div>
          <div>
            <label className="block text-gray-700 text-sm font-semibold mb-2">새로운 비밀번호 (8~16자 이내)</label>
            <input
              type="password"
              className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-300"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="새로운 비밀번호를 입력해주세요."
            />
          </div>
          <div>
            <label className="block text-gray-700 text-sm font-semibold mb-2">새로운 비밀번호 확인</label>
            <input
              type="password"
              className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-300"
              value={confirmNewPassword}
              onChange={(e) => setConfirmNewPassword(e.target.value)}
              placeholder="새로운 비밀번호를 한번 더 입력해주세요."
            />
          </div>
          {passwordChangeError && (
            <p className="text-red-500 text-sm mt-2">{passwordChangeError}</p>
          )}
          <div className="flex justify-end">
            <button
              onClick={handleChangePassword}
              className="px-6 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition duration-300"
            >
              비밀번호 변경
            </button>
          </div>
          {/* 회원 탈퇴 섹션 */}
        <div className="mt-10 pt-8 border-t border-gray-200">
            <h3 className="text-xl font-bold mb-5 text-red-600">회원 탈퇴</h3>
            <p className="text-gray-700 mb-6">
                회원 탈퇴 시 모든 서비스 이용이 중단되며, 회원 정보는 복구할 수 없습니다.
                신중하게 결정해 주세요.
            </p>
            <div className="flex justify-end">
                <button
                    onClick={handleGoToWithdrawal} // ⭐ 페이지 이동 함수 연결 ⭐
                    className="px-6 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 transition duration-300"
                >
                    회원 탈퇴
                </button>
            </div>
        </div>
        </div>
      </div>
    </div>
  );
}

// 정보 표시 및 수정 필드 컴포넌트
function ProfileInfoRow({ label, value, isEditing, editedValue, onEditChange, type = "text" }) {
  if (label === "닉네임") { // 닉네임은 Profile 컴포넌트 내에서 직접 처리하므로 여기서 반환하지 않음
    return null;
  }
  return (
    <div className="mb-4">
      <label className="block text-gray-700 text-sm font-bold mb-2">{label}</label>
      {isEditing && onEditChange ? ( // 수정 모드이고 변경 함수가 있을 때
        <input
          type={type}
          className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          value={editedValue}
          onChange={(e) => onEditChange(e.target.value)}
          readOnly={label === "이메일"} // 이메일은 readOnly로 고정
        />
      ) : ( // 조회 모드일 때
        <p className="text-gray-900 text-lg">{value}</p>
      )}
    </div>
  );
}

export default Profile;