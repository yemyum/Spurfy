import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

function DogRegister() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    breed: '',
    birthDate: '',
    gender: '',
    weight: '',
    notes: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/dogs', form);
      alert('강아지 등록 성공! 🐶✨');
      navigate('/mypage'); // 등록 후 마이페이지로 이동
    } catch (err) {
      console.error(err);
      alert('등록 실패 ㅠ.ㅠ');
    }
  };
  

  return (
    <div style={{ maxWidth: 500, margin: '50px auto' }}>
      <h2>🐾 강아지 등록</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" name="name" placeholder="이름" value={form.name} onChange={handleChange} required />
        <input type="text" name="breed" placeholder="견종" value={form.breed} onChange={handleChange} required />
        <input type="date" name="birthDate" value={form.birthDate} onChange={handleChange} required />
        <select name="gender" value={form.gender} onChange={handleChange} required>
          <option value="">성별 선택</option>
          <option value="M">수컷</option>
          <option value="F">암컷</option>
        </select>
        <input type="number" step="0.1" name="weight" placeholder="몸무게(kg)" value={form.weight} onChange={handleChange} required />
        <textarea name="notes" placeholder="특이사항" value={form.notes} onChange={handleChange}></textarea>
        <button type="submit">등록하기</button>
      </form>
    </div>
  );
}

export default DogRegister;