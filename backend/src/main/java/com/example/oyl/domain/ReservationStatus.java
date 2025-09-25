package com.example.oyl.domain;

public enum ReservationStatus {
    RESERVED,  // 예약 완료
    CANCELED,  // 예약 취소
    COMPLETED  // 이용완료 ← 리뷰 작성 가능 상태!
}
