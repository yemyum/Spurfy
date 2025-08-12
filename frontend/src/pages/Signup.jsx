import React, { useState } from 'react';
import api from '../api/axios';
import { useNavigate } from 'react-router-dom';
import Logo from '../assets/Logo.png';
import SpurfyButton from '../components/Common/SpurfyButton';

function Signup() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        email: '',
        password: '',
        name: '',
        nickname: '',
        phone: '',
    });

    // 중복 체크 상태 및 메시지 관리
    const [emailError, setEmailError] = useState(''); // 이메일 에러 메시지
    const [nicknameError, setNicknameError] = useState(''); // 닉네임 에러 메시지
    const [isNicknameChecked, setIsNicknameChecked] = useState(false); // 닉네임 중복 확인 했는지 여부 (true/false)
    const [isNicknameAvailable, setIsNicknameAvailable] = useState(false); // 닉네임 사용 가능한지 여부 (true/false)
    const [isEmailChecked, setIsEmailChecked] = useState(false); // 이메일 중복 확인 했는지 여부 (추가)
    const [isEmailAvailable, setIsEmailAvailable] = useState(false); // 이메일 사용 가능한지 여부 (추가)

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });

        // 입력값이 변경될 때 관련 에러 메시지 및 중복 체크 상태 초기화
        if (name === 'email') {
            setEmailError('');
            setIsEmailChecked(false);   // 이메일 변경 시 중복 확인 다시 해야 함
            setIsEmailAvailable(false);
        }
        if (name === 'nickname') {
            setNicknameError('');
            setIsNicknameChecked(false); // 닉네임 변경 시 중복 확인 다시 해야 함
            setIsNicknameAvailable(false);
        }

    };

    // 이메일 중복 확인 함수 (버튼 클릭 시 실행)
    const handleCheckEmail = async () => {
        const currentEmail = form.email.trim();

        if (currentEmail === '') {
            setEmailError('이메일을 입력해주세요.');
            setIsEmailAvailable(false);
            setIsEmailChecked(false);
            return;
        }

        try {
            const response = await api.get(`/users/check-email?email=${currentEmail}`);
            if (response.data) { // response.data가 true이면 중복
                setEmailError('이미 사용 중인 이메일이에요.');
                setIsEmailAvailable(false);
            } else {
                setEmailError(''); // 중복 아니면 에러 없음
                setIsEmailAvailable(true);
            }
            setIsEmailChecked(true); // 중복 확인 완료
        } catch (error) {
            console.error('이메일 중복 체크 에러:', error);
            const errorMessage = error.response?.data?.message || '이메일 중복 확인 중 오류가 발생했어요.';
            setEmailError(errorMessage);
            setIsEmailAvailable(false);
            setIsEmailChecked(false);
        }
    };

    // 닉네임 중복 확인 함수 (중복 확인 버튼 누를 때 실행)
    const handleCheckNickname = async () => {
        const currentNickname = form.nickname.trim(); // 현재 닉네임 입력값 가져오기

        if (currentNickname === '') { // 닉네임이 비어있으면
            setNicknameError('닉네임을 입력해주세요.');
            setIsNicknameAvailable(false); // 사용 불가
            setIsNicknameChecked(false); // 확인 안 됨
            return;
        }

        try {
            const res = await api.get(`/mypage/check-nickname?nickname=${currentNickname}`);

            if (res.data.code === 'S001') { // 백엔드 응답 코드가 'S001'이면 정상 응답
                const available = res.data.data; // 실제 중복 여부 (true: 사용 가능, false: 사용 불가)
                setIsNicknameAvailable(available); // 사용 가능 여부 상태 업데이트
                setNicknameError(available ? '' : res.data.message); // 사용 가능하면 에러 없음, 아니면 백엔드 메시지 보여줌
                setIsNicknameChecked(true); // 중복 확인 완료!
            } else { // 'S001' 코드가 아니면 다른 문제 발생
                setNicknameError(res.data.message || '닉네임 확인 중 오류가 발생했습니다.');
                setIsNicknameAvailable(false);
                setIsNicknameChecked(false);
            }
        } catch (err) {
            console.error('닉네임 중복 확인 실패:', err);
            const errorMessage = err.response?.data?.message || '닉네임 확인 중 네트워크 오류가 발생했습니다.';
            setNicknameError(errorMessage);
            setIsNicknameAvailable(false);
            setIsNicknameChecked(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // 최종 유효성 검사 및 중복 확인 여부 체크
        let formIsValid = true; // 폼 전체가 유효한지

        // 1. 필수 입력 필드 검사 (비어있는지 확인)
        if (form.name.trim() === '') {
            alert('이름을 입력해주세요!');
            formIsValid = false;
        } else if (form.email.trim() === '') {
            alert('이메일을 입력해주세요!');
            formIsValid = false;
        } else if (form.password.trim() === '') {
            alert('비밀번호를 입력해주세요!');
            formIsValid = false;
        } else if (form.nickname.trim() === '') {
            alert('닉네임을 입력해주세요!');
            formIsValid = false;
        } else if (form.phone.trim() === '') { // 전화번호도 필수로 한다면
            alert('전화번호를 입력해주세요!');
            formIsValid = false;
        }

        // 2. 이메일 중복 확인 여부 및 사용 가능 여부
        if (formIsValid && (!isEmailChecked || !isEmailAvailable || emailError)) {
            alert('이메일 중복 확인을 완료하고 사용 가능한 이메일을 입력해주세요!');
            formIsValid = false;
        }

        // 3. 닉네임 중복 확인 여부 및 사용 가능 여부
        if (formIsValid && (!isNicknameChecked || !isNicknameAvailable || nicknameError)) {
            alert('닉네임 중복 확인을 완료하고 사용 가능한 닉네임을 입력해주세요!');
            formIsValid = false;
        }

        // 모든 검증 통과 실패 시, 여기서 함수 종료
        if (!formIsValid) {
            return;
        }

        // 모든 검증 통과 후 회원가입 요청
        try {
            const res = await api.post('/users/signup', form);
            alert(res.data.message);
            navigate('/login');
        } catch (err) {
            console.error('회원가입 실패:', err);
            const errorMessage = err.response?.data?.message || '회원가입 중 오류가 발생했어요';
            alert(errorMessage);
        }
    };


    return (
        <div className="bg-gradient-to-br from-white to-[#BAE5FF] min-h-screen select-none">
            <div className="min-h-screen flex flex-col">
                <header className="p-8 flex justify-between items-center">
                    <img
                        src={Logo}
                        alt="Spurfy 로고"
                        className="w-48 h-14 mr-3 cursor-pointer relative z-50"
                        onClick={() => navigate('/')}
                    />
                </header>
                <div className="flex flex-1 items-center justify-center -mt-20">
                    <div className="min-h-[380px] bg-white/60 flex flex-col items-center border border-gray-200 rounded-xl shadow-md p-8 max-w-md">
                        <h2 className="font-logo text-[#9EC5FF] text-2xl mt-4 mb-10">Sign up</h2>
                        <form onSubmit={handleSubmit}>
                            <input
                                type="text"
                                name="name"
                                placeholder="이름"
                                value={form.name}
                                onChange={handleChange}
                                className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mb-4"
                                required
                            />

                            <div className="flex items-center w-full mb-4"> {/* 닉네임 입력 필드와 버튼을 감싸는 div 추가 */}
                                <input
                                    type="text"
                                    name="nickname"
                                    placeholder="닉네임"
                                    value={form.nickname}
                                    onChange={handleChange}
                                    className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mr-2"
                                />
                                <SpurfyButton
                                    variant='outline'
                                    onClick={handleCheckNickname} // 닉네임 중복 확인 버튼 클릭 시 함수 실행
                                    className="whitespace-nowrap px-2 py-2 shadow-sm text-sm"
                                    type="button"
                                >
                                    중복 확인
                                </SpurfyButton>
                            </div>
                            {nicknameError && <p className="text-red-500 text-sm mb-3 ml-1">{nicknameError}</p>}
                            {!nicknameError && isNicknameChecked && isNicknameAvailable && (
                                <p className="text-green-500 text-sm mb-3 ml-1">사용 가능한 닉네임이에요!</p>
                            )}

                            <div className="flex items-center w-full mb-4">
                                <input
                                    type="email"
                                    name="email"
                                    placeholder="Email"
                                    value={form.email}
                                    onChange={handleChange}
                                    className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mr-2"
                                    required
                                />
                                <SpurfyButton
                                    variant='outline'
                                    onClick={handleCheckEmail}
                                    className="whitespace-nowrap px-2 py-2 shadow-sm text-sm"
                                    type="button"
                                >
                                    중복 확인
                                </SpurfyButton>
                            </div>
                            {emailError && <p className="text-red-500 text-sm mb-3 ml-1">{emailError}</p>}
                            {!emailError && isEmailChecked && isEmailAvailable && (
                                <p className="text-green-500 text-sm mb-3 ml-1">사용 가능한 이메일이에요!</p>
                            )}

                            <input
                                type="password"
                                name="password"
                                placeholder="Password"
                                value={form.password}
                                onChange={handleChange}
                                className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mb-4"
                                required
                            />
                            <input
                                type="tel"
                                name="phone"
                                placeholder="전화번호"
                                value={form.phone}
                                onChange={handleChange}
                                className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mb-4"
                            />
                            <SpurfyButton
                                variant='primary'
                                type="submit"
                                className="font-logo text-lg py-2 w-full mt-8"
                            >
                                Sign up
                            </SpurfyButton>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Signup;
