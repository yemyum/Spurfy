import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';

function DogEdit() {
  const { dogId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    breed: '',
    birthDate: '',
    gender: '',
    weight: '',
    notes: '',
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/dogs/${dogId}`)
    
      .then((res) => {
        const dog = res.data.data;
        setForm({
  ...dog,
  birthDate: dog.birthDate?.substring(0, 10),
  notes: dog.notes ?? '', // null/undefined일 때 ""로 세팅
});
      setLoading(false);
    })
      .catch((err) => {
        console.error('불러오기 실패:', err);
        alert('강아지 정보 조회 실패 ㅠ');
        setLoading(false);
      });
  }, [dogId]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.patch(`/dogs/${dogId}`, form);
      alert('수정 완료! ✨');
      navigate('/mypage/dogs');
    } catch (err) {
      console.error(err);
      alert('수정 실패 ㅠ');
    }
  };

  if (loading) return <p>🐾 강아지 정보 불러오는 중...</p>;

  return (
    <div style={{ maxWidth: '500px', margin: '50px auto' }}>
      <h2>🐾 강아지 정보 수정</h2>
      <form onSubmit={handleSubmit}>
        <label>이름
        <input type="text" name="name" value={form.name} onChange={handleChange} required />
        </label>
        <input type="text" name="breed" placeholder="견종" value={form.breed} onChange={handleChange} required />
        <input type="date" name="birthDate" value={form.birthDate} onChange={handleChange} required />
        <select name="gender" value={form.gender} onChange={handleChange} required>
        <option value="">👀 성별 선택</option>
        <option value="M">🐶 수컷</option>
        <option value="F">🐕‍ 암컷</option>
        </select>
        <input type="number" step="0.1" name="weight" placeholder="몸무게(kg)" value={form.weight} onChange={handleChange} required />
        <textarea
         name="notes"
         placeholder="특이사항"
         value={form.notes}
         onChange={handleChange}
         rows={3}
        />

        <button type="submit">
        ✏️ 수정하기
        </button>
      </form>
    </div>
  );
}

export default DogEdit;