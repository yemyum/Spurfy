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
    setForm((prev) => ({ ...prev, [name]: value }))
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/users/login', form);
      const token = res.data.data;

      localStorage.setItem('token', token);
      alert('로그인 성공');

      navigate('/', { replace: true });
    } catch (err) {
      console.error('로그인 실패:', err);
      alert('로그인을 실패하였습니다.');
    }
  };

  return (
    <div className="bg-[#F1FAFF] min-h-screen select-none">
      <div className="min-h-screen flex flex-col">
        {/* 헤더 섹션 */}
        <header className="p-8 flex justify-between items-center">
          <div
            onClick={() => (window.location.href = '/')}
            className="cursor-pointer relative z-50 mr-3 font-logo text-4xl font-bold text-[#67C7FF]"
          >
            SPURFY
          </div>
        </header>

        <div className="flex flex-1 items-center justify-center">
          <div className="min-h-[380px] bg-white/60 flex flex-col items-center border border-gray-200 rounded-xl shadow-md p-8 max-w-sm w-full">
            <h2 className="font-logo text-[#9EC5FF] text-2xl mt-4 mb-10">Sign in</h2>
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

              <SpurfyButton
                variant="primary"
                type="submit"
                className="font-logo text-lg py-2 w-full mt-8 mb-6"
              >
                Login
              </SpurfyButton>

              <div className="w-full text-sm text-gray-500 mb-4 flex flex-col items-center">
                <p className="text-sm text-gray-500 text-center">
                  아직 회원이 아니신가요?
                  <Link to="/signup" className="font-semibold hover:underline ml-1">
                    회원가입하기
                  </Link>
                </p>
              </div>

            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;