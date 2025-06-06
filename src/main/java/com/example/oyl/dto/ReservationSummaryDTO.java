package com.example.oyl.dto;

import com.example.oyl.domain.RefundStatus;
import com.example.oyl.domain.ReservationStatus;
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
    private ReservationStatus reservationStatus;        // "RESERVED" or "CANCELED"
    private RefundStatus refundStatus;                  // "NONE", "WAITING", "COMPLETED", "REJECTED"

    // 생성자 명시적으로 추가해주기
    public ReservationSummaryDTO(String reservationId, String userNickname, String dogName,
                                 String serviceName, LocalDate reservationDate, LocalTime reservationTime,
                                 ReservationStatus reservationStatus, RefundStatus refundStatus) {
        this.reservationId = reservationId;
        this.userNickname = userNickname;
        this.dogName = dogName;
        this.serviceName = serviceName;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
        this.refundStatus = refundStatus;
    }
}
