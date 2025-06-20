import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

function MyDogs() {
  const navigate = useNavigate();
  const [dogs, setDogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDogs = async () => {
      try {
        const res = await api.get('/dogs'); // 토큰 자동 실림
        setDogs(res.data.data);
      } catch (err) {
        console.error('🐾 강아지 목록 조회 실패:', err);
        alert('불러오기 실패 ㅠㅠ');
      } finally {
        setLoading(false);
      }
    };

    fetchDogs();
  }, []);

  const handleDelete = async (dogId, dogName) => {
    if (!window.confirm(`정말 ${dogName}을(를) 삭제할까요? 🥺`)) return;

    try {
      await api.delete(`/dogs/${dogId}`);
      alert('삭제 완료!');
      setDogs(dogs.filter((dog) => dog.dogId !== dogId));
    } catch (err) {
      console.error('❌ 삭제 실패:', err);
      alert('삭제 중 오류가 발생했어요!');
    }
  };

  if (loading) return <p>🐾 강아지 목록 불러오는 중...</p>;

  return (
    <div style={{ maxWidth: '600px', margin: 'auto' }}>
      <h2>🐾 반려견 케어</h2>
      <h2>🐶 나의 반려견 리스트</h2>

      <div style={{ textAlign: 'right', marginBottom: '10px' }}>
      <button onClick={() => navigate('/dogs/register')}>+ 강아지 등록</button>
      </div>

      {dogs.length === 0 ? (
        <p>등록된 강아지가 없어요!</p>
      ) : (
        <ul>
          {dogs.map((dog) => (
            <li key={dog.dogId} style={{ borderBottom: '1px solid #ccc', marginBottom: '10px', padding: '10px' }}>
              <h3>{dog.name}</h3>
              <p>견종: {dog.breed}</p>
              <p>생일: {dog.birthDate}</p>
              <p>성별: {dog.gender === 'M' ? '수컷' : '암컷'}</p>
              <p>몸무게: {dog.weight}kg</p>
              <p>특이사항: {dog.notes?.trim().length > 0 ? dog.notes : '없음'}</p>
              <div style={{ marginTop: '10px' }}>
                <button onClick={() => navigate(`/dogs/${dog.dogId}/edit`)}>수정</button>
                <button
                  style={{ marginLeft: '10px', color: 'red' }}
                  onClick={() => handleDelete(dog.dogId, dog.name)}
                >
                  삭제
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default MyDogs;