// 실행 방법: powershell -> k6 run "C:\Users\smile\IdeaProjects\Spurfy\backend\load-test\reservation-test.js"

import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 50,          // 동시에 50명
  iterations: 50,   // 총 50번 요청
};

export default function () {

  let payload = JSON.stringify({
    dogId: "실제 강아지 ID",
    serviceId: "spa_004",
    reservationDate: "2026-03-10",
    reservationTime: "10:00",
    amount: 39000,
    paymentMethod: "CARD"
  });

  let params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer 실제 액세스 토큰'
    },
  };

  let res = http.post('http://localhost:8080/api/reservation/pay', payload, params);

  console.log(res.status);

  check(res, {
    'status is 200 or 400': (r) => r.status === 200 || r.status === 400,
  });
}