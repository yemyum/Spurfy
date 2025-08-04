# 🐶 Spurfy - 반려견 힐링 스파 예약 시스템

> “It’s a dog spa, Spurfy!”  
> 반려견을 위한 감성 스파 예약 서비스를 중심으로,
AI 챗봇을 통해 사용자의 반려견 사진을 분석하여 서비스 추천을 지원하는
대화형 사용자 경험 기반의 스파 플랫폼입니다.

---

## 🧼 주요 기능

### 👤 사용자
- 회원가입 / 로그인 (JWT 인증)
- 반려견 등록 / 예약 / 리뷰 작성
- 스파 서비스 목록 조회
- 마이페이지 예약 확인
- 리뷰 작성 및 이미지 업로드
- AI가 반려견 사진을 분석해 견종별 맞춤 스파를 추천

---

## 🧱 기술 스택

| 구분 | 기술 |
|------|------|
| 백엔드 | Spring Boot 3, Spring Security, JPA+MyBatis |
| 프론트 | React (Vite), TailwindCSS |
| 챗봇 연동 | GPT API, Google Vision API 등 |
| 데이터베이스 | MySQL |
| 기타 도구 | Postman, Notion, Miro |

---

## 📊 ERD 요약

- 사용자(users), 반려견(dogs), 스파 서비스(spa_services), 예약(reservations), 리뷰(reviews)
- 사용자 1:N 강아지, 예약 1:1 리뷰 구조

👉 ERD 및 상세 SQL은 Notion 참고: https://www.notion.so/SPURFY-SOLO-PROJECT-1-206023034fb1806a9cabd9031a1a02d1?source=copy_link

---

## 🔗 API 요약

RESTful API + JWT 기반 인증

예시:
- `POST /api/users/signup`: 회원가입
- `POST /api/reservations`: 예약 생성
- `PATCH /api/reservations/{id}/cancel`: 예약 취소
- `POST /api/admin/services`: 관리자 서비스 등록

요청/응답 JSON, 에러 핸들링 등 포함 👉 https://www.notion.so/SPURFY-SOLO-PROJECT-3-208023034fb18058b380ed586cce2bbc?source=copy_link

---

## 🚀 실행 방법

```bash
# 백엔드 실행
./mvnw spring-boot:run

# 프론트 (vite) 실행
npm install
npm run dev
