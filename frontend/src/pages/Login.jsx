import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Logo from '../assets/Logo.png';
import SpurfyButton from '../components/Common/SpurfyButton';

function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: '',
    password: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/users/login', form);
      const token = res.data.data;

      localStorage.setItem('token', token);
      alert('로그인 성공');
      navigate('/');
    } catch (err) {
      console.error('로그인 실패:', err);
      alert('로그인을 실패하였습니다.');
    }
  };

  return (
    <div className="bg-gradient-to-br from-white to-[#BAE5FF] min-h-screen select-none">
    <div className="min-h-screen flex flex-col">
      {/* 헤더 섹션 */}
      <header className="p-8 flex justify-between items-center">
            <img
              src={Logo}
              alt="Spurfy 로고"
              className="w-48 h-14 mr-3 cursor-pointer relative z-50"
              onClick={() => navigate('/')}
            />
      </header>

      <div className="flex flex-grow items-center justify-center gap-x-64 p-4 -mt-20">
        {/* 설명글 div */}
        <div className="text-left font-logo text-stone-600 text-3xl max-w-sm">
          <p>소중한 <span className="text-spurfyAI">반려견</span>을 위한</p>
          <p><span className="text-spurfyBlue">힐링 스파</span> 입니다.</p>
        </div>

    <div className="min-h-[380px] bg-white/60 flex flex-col items-center border border-gray-200 rounded-xl shadow-md p-8 max-w-xs">
      <h2 className="font-logo text-[#9EC5FF] text-2xl mt-4 mb-10">Sing in</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mb-4"
          required
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          className="w-full bg-[#E2F3FF] rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-100 mb-4"
          required
        />

        <div className="flex justify-between w-full text-sm text-gray-500 mb-4"> {/* mb-4로 버튼과 간격, w-full로 가로 꽉 채우기 */}
            <Link to="/signup" className="hover:underline"> {/* 회원가입 페이지 링크 */}
              <span className="text-xs text-spurfyBlue font-bold">&lt;</span> 회원가입
            </Link>
            <Link to="/find-password" className="hover:underline"> {/* 비밀번호 찾기 페이지 링크 */}
              이메일/PW 찾기 <span className="text-xs text-spurfyBlue font-bold">&gt;</span>
            </Link>
          </div>

        <SpurfyButton 
          variant='primary' 
          type="submit"
          className="font-logo text-lg py-2 w-full mt-8"
        >
          Login
        </SpurfyButton>
      </form>
      </div>
    </div>
  </div>
</div>
  );
}

export default Login;