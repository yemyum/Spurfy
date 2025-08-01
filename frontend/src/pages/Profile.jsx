import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'; // ⭐ 아이콘 사용을 위해 추가
import { faCamera } from '@fortawesome/free-solid-svg-icons'; // ⭐ 카메라 아이콘 추가
import SpurfyButton from '../components/Common/SpurfyButton';

function Profile() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null); // 현재 조회된 사용자 프로필

  // 수정 모드 관련 상태
  const [isEditing, setIsEditing] = useState(false); // 수정 모드 여부
  const [editedNickname, setEditedNickname] = useState(''); // 수정 중인 닉네임
  const [editedName, setEditedName] = useState(''); // 수정 중인 이름
  const [editedPhone, setEditedPhone] = useState(''); // 수정 중인 전화번호
  // const [profileImageFile, setProfileImageFile] = useState(null); // ⭐ 이미지 파일 상태 (추후 업로드 시 사용)
  // const [previewImageUrl, setPreviewImageUrl] = useState(''); // ⭐ 이미지 미리보기 URL (추후 업로드 시 사용)

  // 닉네임 중복 확인 관련 상태
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
        setProfile(null); // 에러 발생 시에도 profile을 null로 유지
      }
    };

    fetchProfile();
  }, []);

  const handleGoToWithdrawal = () => {
    navigate('/mypage/withdrawal');
  };

  const handleCheckNickname = async () => {
    if (editedNickname.trim() === '') {
      setNicknameCheckMessage('닉네임을 입력해주세요.');
      setIsNicknameAvailable(false);
      setIsNicknameChecked(false);
      return;
    }

    if (profile && editedNickname === profile.nickname) {
        setNicknameCheckMessage('현재 사용 중인 닉네임입니다.');
        setIsNicknameAvailable(true);
        setIsNicknameChecked(true);
        return;
    }

    try {
      const res = await api.get(`/mypage/check-nickname?nickname=${editedNickname}`);
      if (res.data.code === 'S001') {
        const available = res.data.data;
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
      const errorMessage = err.response?.data?.message || '닉네임 확인 중 네트워크 오류가 발생했습니다.';
      setNicknameCheckMessage(errorMessage);
      setIsNicknameAvailable(false);
      setIsNicknameChecked(false);
    }
  };

  const handleUpdateProfile = async () => {
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
      const updateData = {
        nickname: editedNickname,
        name: editedName,
        phone: editedPhone,
      };

      const res = await api.put('/mypage/profile', updateData);

      if (res.data.code === 'S001') {
        alert('프로필이 성공적으로 수정되었습니다!');
        setProfile(res.data.data); // 서버에서 받은 최신 정보로 프로필 상태 업데이트
        setIsEditing(false);
        setIsNicknameAvailable(true);
        setIsNicknameChecked(true);
        setNicknameCheckMessage('변경사항이 저장되었습니다.');
      } else {
        alert(res.data.message || '프로필 수정에 실패했습니다.');
      }
    } catch (err) {
      console.error("프로필 수정 실패:", err);
      const errorMessage = err.response?.data?.message || '프로필 수정 중 네트워크 오류가 발생했습니다.';
      alert(errorMessage); // 사용자에게 에러 메시지 보여줌
      if (err.response?.data?.code === 'U002') {
          setNicknameCheckMessage(errorMessage);
          setIsNicknameAvailable(false);
          setIsNicknameChecked(true);
      }
    }
  };

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
      const res = await api.put('/mypage/password', {
        currentPassword,
        newPassword,
        confirmPassword: confirmNewPassword,
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

  const handleNicknameChange = (e) => {
    setEditedNickname(e.target.value);
    // 닉네임 변경 시 중복 확인 상태 초기화
    setIsNicknameAvailable(false);
    setNicknameCheckMessage('');
    setIsNicknameChecked(false); // 다시 중복 확인 필요
  };

  if (!profile) {
    return null;
  }

  return (
    <div className="mx-auto p-8 select-none">
      <h2 className="text-2xl font-bold mb-6 text-spurfyBlue">내 프로필</h2>

      {/* 프로필 이미지 */}
      <div className="flex flex-col items-center justify-center mb-6 relative mt-20">
        <div className="w-32 h-32 rounded-full border-2 border-gray-300 flex items-center justify-center overflow-hidden bg-gray-100">
          {/* ⭐ 실제 프로필 이미지가 있다면 여기에 img 태그 사용 ⭐ */}
          {/* profile.profileImageUrl 또는 previewImageUrl 사용 */}
          {/* 예시: <img src={previewImageUrl || defaultProfile} alt="프로필 이미지" className="w-full h-full object-cover" /> */}
        </div>
        <button className="w-50 px-2 py-1 mt-2 text-gray-500 font-semibold rounded-md shadow-sm border border-gray-200 bg-white hover:bg-gray-50">
          <FontAwesomeIcon icon={faCamera} /> 사진 편집하기
        </button>
        {/* ⭐ 이미지 파일 선택 input (hidden으로 만들고 버튼 클릭 시 트리거) ⭐ */}
        {/* <input type="file" accept="image/*" className="hidden" onChange={handleImageChange} /> */}
      </div>

            {/* 정보 리스트 또는 수정 폼 */}
      <div className="space-y-6 mt-20">
        <h3 className="text-xl font-bold text-spurfyBlue mb-5">내 정보</h3>
        {/* 1. 이름 정보 */}
        <div className="mb-4">
          <ProfileInfoRow label="이름" value={profile.name} isEditing={isEditing} editedValue={editedName} onEditChange={setEditedName} type="text" />
          <div className="border-t-2 border-gray-200 w-full mt-2"></div>
        </div>

        {/* 2. 닉네임 정보 */}
        <div className="mb-4">
          <label className="block text-gray-800 font-semibold mb-2">닉네임</label>
          {isEditing ? ( // 수정 모드일 때
            <div className="flex items-center gap-2">
              <input
                type="text"
                className="appearance-none w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none"
                value={editedNickname}
                onChange={handleNicknameChange}
                placeholder="새 닉네임을 입력하세요."
              />
              <SpurfyButton variant='outline'
                onClick={handleCheckNickname}
                className="whitespace-nowrap px-3 py-2 shadow-sm text-sm"
              >
                중복 확인
              </SpurfyButton>
            </div>
          ) : ( // 조회 모드일 때
            <p className="text-gray-900 text-lg">{profile.nickname}</p>
          )}
          <div className="border-t-2 border-gray-200 w-full mt-2"></div>
          {/* 닉네임 중복 확인 메시지 표시 */}
          {isEditing && nicknameCheckMessage && (
            <p className={`mt-2 ${isNicknameAvailable ? 'text-green-600' : 'text-red-600'}`}>
              {nicknameCheckMessage}
            </p>
          )}
        </div>

        {/* 3. 전화번호 정보 */}
        <div className="mb-4">
          <ProfileInfoRow label="전화번호" value={profile.phone} isEditing={isEditing} editedValue={editedPhone} onEditChange={setEditedPhone} type="tel" />
          <div className="border-t-2 border-gray-200 w-full mt-2"></div>
        </div>
        
        {/* 4. 이메일 정보 */}
        <div className="mb-4">
          <ProfileInfoRow label="이메일" value={profile.email} isEditing={false} type="email" /> {/* 이메일은 수정 불가 */}
          <div className="border-t-2 border-gray-200 w-full mt-2"></div>
        </div>
      </div>

      {/* 프로필 수정/저장/취소 버튼 */}
      <div className="mt-8 flex justify-end gap-4">
        {isEditing ? (
          <>
            <SpurfyButton
              onClick={handleUpdateProfile}
              className="px-4 py-2 shadow-sm"
            >
              저장하기
            </SpurfyButton>
            <button
              onClick={() => {  // 컴포넌트는 유지하고 모드만 전환되는 경우!
                setIsEditing(false);
                setEditedNickname(profile.nickname);
                setEditedName(profile.name);
                setEditedPhone(profile.phone);
                // setProfileImageFile(null); // ⭐ 이미지 파일 초기화
                // setPreviewImageUrl(profile.profileImageUrl || defaultProfile); // ⭐ 이미지 미리보기 초기화
              }}
              className="px-4 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg shadow-sm hover:bg-gray-300 transition duration-300"
            >
              취소
            </button>
          </>
        ) : (
          <SpurfyButton variant='outline'
            onClick={() => setIsEditing(true)}
            className="px-4 py-2 shadow-sm"
          >
            프로필 수정
          </SpurfyButton>
        )}
      </div>

      {/* 비밀번호 변경 섹션 */}
      <div className="mt-4 pt-8 border-t border-gray-200">
        <h3 className="text-xl font-bold mb-5 text-spurfyBlue">비밀번호 변경</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-gray-700 font-semibold mb-2">현재 비밀번호</label>
            <input
              type="password"
              className="w-full p-3 bg-gray-50 rounded-md focus:outline-none"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="현재 비밀번호를 입력해주세요."
            />
          </div>
          <div>
            <label className="block text-gray-700 font-semibold mb-2">새로운 비밀번호 (8~16자 이내)</label>
            <input
              type="password"
              className="w-full p-3 bg-gray-50 rounded-md focus:outline-none"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="새로운 비밀번호를 입력해주세요."
            />
          </div>
          <div>
            <label className="block text-gray-700 font-semibold mb-2">새로운 비밀번호 확인</label>
            <input
              type="password"
              className="w-full p-3 bg-gray-50 rounded-md focus:outline-none"
              value={confirmNewPassword}
              onChange={(e) => setConfirmNewPassword(e.target.value)}
              placeholder="새로운 비밀번호를 한번 더 입력해주세요."
            />
          </div>
          {passwordChangeError && (
            <p className="text-red-500 text-sm mt-2">{passwordChangeError}</p>
          )}
          <div className="flex justify-end">
            <SpurfyButton variant='outline'
              onClick={handleChangePassword}
              className="px-3 py-2 shadow-sm"
            >
              비밀번호 변경
            </SpurfyButton>
          </div>
          {/* 회원 탈퇴 섹션 */}
        <div className="mt-4 pt-8 border-t border-gray-200">
            <h3 className="text-xl font-bold text-gray-800 mb-5">회원 탈퇴</h3>
            <p className="text-gray-400 font-semibold mb-6">
                회원 탈퇴 시 모든 서비스 이용이 중단되며, 회원 정보는 복구할 수 없습니다.
                신중하게 결정해 주세요.
            </p>
            <div className="flex justify-end">
                <SpurfyButton variant = "danger"
                    onClick={handleGoToWithdrawal}
                    className="px-6 py-2 shadow-sm"
                >
                    회원 탈퇴
                </SpurfyButton>
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
    <div>
      <label className="block text-gray-800 font-semibold mb-2">{label}</label>
      {isEditing && onEditChange ? (
        <input
          type={type}
          className="appearance-none w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none"
          value={editedValue}
          onChange={(e) => onEditChange(e.target.value)}
          readOnly={label === "이메일"} // 이메일은 readOnly로 고정
        />
      ) : (
        <p className="text-gray-900 text-lg">{value}</p>
      )}
    </div>
  );
}

export default Profile;