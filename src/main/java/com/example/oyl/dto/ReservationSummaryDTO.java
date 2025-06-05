package com.example.oyl.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class ReservationSummaryDTO {
    private String reservationId;
    private String userNickname;          // 사용자 닉네임
    private String dogName;               // 강아지 이름
    private String serviceName;           // 스파 서비스 이름
    private LocalDate reservationDate;    // 예약 날짜
    private LocalTime reservationTime;    // 예약 시간
    private int reservationStatus;        // 1: 완료, 2: 취소
    private int refundStatus;             // 0: 없음, 1: 대기, 2: 완료, 3: 거절

    // ✅ ⭐ 생성자 명시적으로 추가!!
    public ReservationSummaryDTO(String reservationId, String userNickname, String dogName,
                                 String serviceName, LocalDate reservationDate, LocalTime reservationTime,
                                 int reservationStatus, int refundStatus) {
        this.reservationId = reservationId;
        this.userNickname = userNickname;
        this.dogName = dogName;
        this.serviceName = serviceName;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
        this.refundStatus = refundStatus;
    }

    // 👉 (선택) 상태를 텍스트로 반환하는 메서드도 가능
    public String getReservationStatusLabel() {
        return switch (reservationStatus) {
            case 1 -> "예약완료";
            case 2 -> "취소됨";
            default -> "알 수 없음";
        };
    }

    public String getRefundStatusLabel() {
        return switch (refundStatus) {
            case 0 -> "환불 없음";
            case 1 -> "환불 대기";
            case 2 -> "환불 완료";
            case 3 -> "환불 거절";
            default -> "알 수 없음";
        };
    }
}
