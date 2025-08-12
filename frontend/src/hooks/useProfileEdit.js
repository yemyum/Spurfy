import { useState, useEffect } from 'react';
import api from '../api/axios';

const useProfileEdit = (navigate) => {
    const [profile, setProfile] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [editedNickname, setEditedNickname] = useState('');
    const [editedName, setEditedName] = useState('');
    const [editedPhone, setEditedPhone] = useState('');

    const [nicknameCheckMessage, setNicknameCheckMessage] = useState('');
    const [isNicknameAvailable, setIsNicknameAvailable] = useState(false);
    const [isNicknameChecked, setIsNicknameChecked] = useState(false);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await api.get('/mypage/profile');
                if (res.data.code === 'S001') {
                    const fetchedProfile = res.data.data;
                    setProfile(fetchedProfile);
                    setEditedNickname(fetchedProfile.nickname);
                    setEditedName(fetchedProfile.name);
                    setEditedPhone(fetchedProfile.phone);
                    setIsNicknameAvailable(true);
                    setIsNicknameChecked(true);
                    setNicknameCheckMessage('현재 사용 중인 닉네임입니다.');
                } else {
                    alert(res.data.message || '프로필 정보를 불러오는데 실패했습니다.');
                }
            } catch (err) {
                console.error('프로필 조회 실패:', err);
                alert(err.response?.data?.message || '프로필을 불러오지 못했어요 😢');
                setProfile(null);
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
                setNicknameCheckMessage(res.data.message);
                setIsNicknameChecked(true);
            } else {
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
        if (isEditing && editedNickname !== profile.nickname && !isNicknameChecked) {
            alert("변경하려는 닉네임에 대해 중복 확인을 해주세요.");
            return;
        }
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
                setProfile(res.data.data);
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
            alert(errorMessage);
            if (err.response?.data?.code === 'U002') {
                setNicknameCheckMessage(errorMessage);
                setIsNicknameAvailable(false);
                setIsNicknameChecked(true);
            }
        }
    };

    const handleNicknameChange = (e) => {
        setEditedNickname(e.target.value);
        setIsNicknameAvailable(false);
        setNicknameCheckMessage('');
        setIsNicknameChecked(false);
    };

    return {
        profile,
        setProfile,
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
    };
};

export default useProfileEdit;