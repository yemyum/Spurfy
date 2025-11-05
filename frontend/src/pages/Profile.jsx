import React from 'react';
import { useNavigate } from 'react-router-dom';
import useProfileEdit from '../hooks/useProfileEdit'; 
import usePasswordChange from '../hooks/usePasswordChange';
import ProfileInfoSection from '../components/Common/ProfileInfoSection';
import PasswordChangeSection from '../components/Common/PasswordChangeSection';
import WithdrawalSection from '../components/Common/WithdrawalSection';

function Profile() {
  const navigate = useNavigate();

  const {
    profile,
    setProfile, // 프로필 수정 후 상태 업데이트를 위해 필요할 수 있음
    isEditing,
    setIsEditing,
    editedNickname,
    setEditedNickname,
    editedName,
    setEditedName,
    editedPhone,
    setEditedPhone,
    nicknameCheckMessage,
    isNicknameAvailable,
    isNicknameChecked,
    handleGoToWithdrawal,
    handleCheckNickname,
    handleUpdateProfile,
    handleNicknameChange,
  } = useProfileEdit(navigate);

  const {
    currentPassword,
    setCurrentPassword,
    newPassword,
    setNewPassword,
    confirmNewPassword,
    setConfirmNewPassword,
    passwordChangeError,
    handleChangePassword,
  } = usePasswordChange();

  if (!profile) {
    return null;
  }

  return (
    <div className="mx-auto p-6 select-none">
      <h2 className="text-2xl font-semibold mb-6 text-spurfyBlue">내 프로필</h2>

      {/* ⭐️ 컴포넌트를 props와 함께 불러오기! ⭐️ */}
      <ProfileInfoSection 
        profile={profile} 
        isEditing={isEditing} 
        setIsEditing={setIsEditing} 
        editedNickname={editedNickname} 
        setEditedNickname={setEditedNickname} 
        editedName={editedName} 
        setEditedName={setEditedName} 
        editedPhone={editedPhone} 
        setEditedPhone={setEditedPhone} 
        nicknameCheckMessage={nicknameCheckMessage} 
        isNicknameAvailable={isNicknameAvailable} 
        handleNicknameChange={handleNicknameChange} 
        handleCheckNickname={handleCheckNickname} 
        handleUpdateProfile={handleUpdateProfile} 
      />

      <PasswordChangeSection 
        currentPassword={currentPassword} 
        setCurrentPassword={setCurrentPassword} 
        newPassword={newPassword} 
        setNewPassword={setNewPassword} 
        confirmNewPassword={confirmNewPassword} 
        setConfirmNewPassword={setConfirmNewPassword} 
        passwordChangeError={passwordChangeError} 
        handleChangePassword={handleChangePassword} 
      />

      <WithdrawalSection handleGoToWithdrawal={handleGoToWithdrawal} />
    </div>
  );
}

export default Profile;