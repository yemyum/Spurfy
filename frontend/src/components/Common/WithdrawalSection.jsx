import React from 'react';
import SpurfyButton from './SpurfyButton';

const WithdrawalSection = ({ handleGoToWithdrawal }) => {
    return (
        <section className="mt-4 pt-8 border-t border-gray-200">
            <h3 className="text-xl font-bold text-gray-800 mb-5">회원 탈퇴</h3>
            <p className="text-gray-400 font-semibold mb-6">
                회원 탈퇴 시 모든 서비스 이용이 중단되며, 회원 정보는 복구할 수 없습니다.
                신중하게 결정해 주세요.
            </p>
            <div className="flex justify-end">
                <SpurfyButton variant="danger"
                    onClick={handleGoToWithdrawal}
                    className="px-6 py-2 shadow-sm"
                >
                    회원 탈퇴
                </SpurfyButton>
            </div>
        </section>
    );
};

export default WithdrawalSection;