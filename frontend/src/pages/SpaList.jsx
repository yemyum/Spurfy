// 📁 src/pages/SpaList.jsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

function SpaList() {
  const [list, setList] = useState([]);

  useEffect(() => {
    api.get('/spa-services')
      .then((res) => setList(res.data.data))
      .catch(() => alert('목록 불러오기 실패🐽'));
  }, []);

  return (
    <div>
      <h2>스파 서비스 목록</h2>
      <ul>
        {list.map((spa) => (
          <li key={spa.serviceId}>
            <Link to={`/spalist/${spa.serviceId}`}>
              <strong>{spa.name}</strong> - {spa.price.toLocaleString()}원 / {spa.durationMinutes}분
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default SpaList;