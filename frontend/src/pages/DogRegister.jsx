import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

function DogRegister() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '',
    breed: '',
    customBreed: '',
    birthDate: '',
    gender: 'M',
    weight: '',
    notes: ''
  });

  const breedOptions = [
    '말티즈', '푸들', '포메라니안', '시츄', '골든리트리버', '치와와', '비숑프리제', '웰시코기', '기타'
  ];

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const finalBreed = form.breed === '기타' ? form.customBreed.trim() : form.breed;

    if (!finalBreed) {
      alert('견종을 입력해주세요!');
      return;
    }

    try {
      await api.post('/dogs', {
        name: form.name,
        breed: finalBreed,
        birthDate: form.birthDate,
        gender: form.gender,
        weight: parseFloat(form.weight),
        notes: form.notes
      });

      alert('등록 완료!');
      navigate('/mypage/dogs');
    } catch (err) {
      console.error('등록 실패:', err);
      alert('오류 발생 🥲');
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: 'auto' }}>
      <h2>🐾 강아지 등록</h2>
      <form onSubmit={handleSubmit}>
        <label>
          이름:
          <input type="text" name="name" value={form.name} onChange={handleChange} required />
        </label>

        <br />

        <label>
          견종:
          <select name="breed" value={form.breed} onChange={handleChange} required>
            <option value="">선택해주세요</option>
            {breedOptions.map((breed) => (
              <option key={breed} value={breed}>{breed}</option>
            ))}
          </select>
        </label>

        {form.breed === '기타' && (
          <>
            <br />
            <label>
              직접 입력:
              <input
                type="text"
                name="customBreed"
                value={form.customBreed}
                onChange={handleChange}
                placeholder="예: 믹스견, 도사견 등"
                required
              />
            </label>
          </>
        )}

        <br />

        <label>
          생년월일:
          <input type="date" name="birthDate" value={form.birthDate} onChange={handleChange} required />
        </label>

        <br />

        <label>
          성별:
          <select name="gender" value={form.gender} onChange={handleChange}>
            <option value="M">수컷</option>
            <option value="F">암컷</option>
          </select>
        </label>

        <br />

        <label>
          몸무게 (kg):
          <input type="number" step="0.1" name="weight" value={form.weight} onChange={handleChange} required />
        </label>

        <br />

        <label>
          특이사항:
          <textarea name="notes" value={form.notes} onChange={handleChange} placeholder="ex) 피부가 약해요" />
        </label>

        <br /><br />
        <button type="submit">등록하기</button>
      </form>
    </div>
  );
}

export default DogRegister;