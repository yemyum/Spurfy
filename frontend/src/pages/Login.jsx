import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

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

      localStorage.setItem('token', token); // ✅ JWT 저장
      alert('로그인 성공! 🐽💗');
      navigate('/mypage'); // 마이페이지 등 원하는 경로로 이동!
    } catch (err) {
      console.error('로그인 실패:', err);
      alert('로그인 실패! 다시 확인해줘 💦');
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '50px auto' }}>
      <h2>로그인</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          name="email"
          placeholder="이메일"
          value={form.email}
          onChange={handleChange}
          required
        />
        <br />
        <input
          type="password"
          name="password"
          placeholder="비밀번호"
          value={form.password}
          onChange={handleChange}
          required
        />
        <br />
        <button type="submit">로그인</button>
      </form>
    </div>
  );
}

export default Login;