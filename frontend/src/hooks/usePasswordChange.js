import { useState } from 'react';
import api from '../api/axios';

const usePasswordChange = () => {
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [passwordChangeError, setPasswordChangeError] = useState(null);

    const handleChangePassword = async () => {
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
            const res = await api.put('/users/me/password', {
                currentPassword,
                newPassword,
                confirmPassword: confirmNewPassword,
            });

            if (res.data.code === 'S001') {
                alert('비밀번호가 성공적으로 변경되었습니다!');
                setCurrentPassword('');
                setNewPassword('');
                setConfirmNewPassword('');
                setPasswordChangeError(null);
            } else {
                setPasswordChangeError(res.data.message || '비밀번호 변경에 실패했습니다.');
            }
        } catch (err) {
            console.error("비밀번호 변경 실패:", err);
            setPasswordChangeError(err.response?.data?.message || '네트워크 오류가 발생했습니다.');
        }
    };

    return {
        currentPassword,
        setCurrentPassword,
        newPassword,
        setNewPassword,
        confirmNewPassword,
        setConfirmNewPassword,
        passwordChangeError,
        handleChangePassword,
    };
};

export default usePasswordChange;