import React from 'react';
import SpurfyButton from './SpurfyButton'; // 너의 SpurfyButton 컴포넌트
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCamera } from '@fortawesome/free-solid-svg-icons';
import ProfileInfoRow from './ProfileInfoRow';

// 필요한 props를 받아와서 사용
const ProfileInfoSection = ({
    profile,
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
    handleNicknameChange,
    handleCheckNickname,
    handleUpdateProfile
}) => {
    return (
        <section>
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
                    <label className="block text-gray-800 font-semibold">닉네임</label>
                    {isEditing ? ( // 수정 모드일 때
                        <div className="flex justify-between">
                            <input
                                type="text"
                                className="appearance-none w-full text-gray-800 leading-tight focus:outline-none"
                                value={editedNickname}
                                onChange={handleNicknameChange}
                                placeholder="새 닉네임을 입력하세요."
                            />
                            <SpurfyButton variant='outline'
                                onClick={handleCheckNickname}
                                className="whitespace-nowrap px-3 py-2 shadow-sm text-sm mb-2"
                            >
                                중복 확인
                            </SpurfyButton>
                        </div>
                    ) : ( // 조회 모드일 때
                        <p className="text-gray-900 text-lg">{profile.nickname}</p>
                    )}
                    <div className="border-t-2 border-gray-200 w-full"></div>
                    {/* 닉네임 중복 확인 메시지 표시 */}
                    {isEditing && nicknameCheckMessage && (
                        <p className={`mt-2 ${isNicknameAvailable ? 'text-green-500' : 'text-red-500'}`}>
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
            <div className="mt-4 flex justify-between">
                {isEditing ? (
                    <>
                        <button
                            onClick={() => {  // 컴포넌트는 유지하고 모드만 전환되는 경우!
                                setIsEditing(false);
                                setEditedNickname(profile.nickname);
                                setEditedName(profile.name);
                                setEditedPhone(profile.phone);
                                // setProfileImageFile(null); // ⭐ 이미지 파일 초기화
                                // setPreviewImageUrl(profile.profileImageUrl || defaultProfile); // ⭐ 이미지 미리보기 초기화
                            }}
                            className="px-6 py-2 font-semibold bg-gray-200 text-gray-600 rounded-lg hover:bg-gray-300 transition duration-300"
                        >
                            취소
                        </button>
                        <SpurfyButton
                            onClick={handleUpdateProfile}
                            className="px-4 py-2"
                        >
                            저장하기
                        </SpurfyButton>
                    </>
                ) : (
                    <div className="flex justify-end w-full">
                        <SpurfyButton variant='outline'
                            onClick={() => setIsEditing(true)}
                            className="px-4 py-2 shadow-sm"
                        >
                            프로필 수정
                        </SpurfyButton>
                    </div>
                )}
            </div>
        </section>
    );
};

export default ProfileInfoSection;