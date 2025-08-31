import React from 'react';
import SpurfyButton from './SpurfyButton';

const PasswordChangeSection = ({
    currentPassword,
    setCurrentPassword,
    newPassword,
    setNewPassword,
    confirmNewPassword,
    setConfirmNewPassword,
    passwordChangeError,
    handleChangePassword,
}) => {
    return (
        <section className="mt-4 pt-8 border-t border-gray-200">
            <h3 className="text-xl font-bold mb-5">비밀번호 변경</h3>
            <div className="space-y-4">
                <div>
                    <label className="block text-gray-700 font-semibold mb-2">현재 비밀번호</label>
                    <input
                        type="password"
                        className="w-full p-3 bg-gray-50 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-50"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                        placeholder="현재 비밀번호를 입력해주세요."
                    />
                </div>
                <div>
                    <label className="block text-gray-700 font-semibold mb-2">새로운 비밀번호 (8~16자 이내)</label>
                    <input
                        type="password"
                        className="w-full p-3 bg-gray-50 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-50"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        placeholder="새로운 비밀번호를 입력해주세요."
                    />
                </div>
                <div>
                    <label className="block text-gray-700 font-semibold mb-2">새로운 비밀번호 확인</label>
                    <input
                        type="password"
                        className="w-full p-3 bg-gray-50 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-50"
                        value={confirmNewPassword}
                        onChange={(e) => setConfirmNewPassword(e.target.value)}
                        placeholder="새로운 비밀번호를 한번 더 입력해주세요."
                    />
                </div>
                {passwordChangeError && (
                    <p className="text-red-400 text-sm mt-2">{passwordChangeError}</p>
                )}
                <div className="flex justify-end">
                    <SpurfyButton variant='outline'
                        onClick={handleChangePassword}
                        className="px-3 py-2 shadow-sm"
                    >
                        비밀번호 변경
                    </SpurfyButton>
                </div>
            </div>
        </section>
    );
};

export default PasswordChangeSection;