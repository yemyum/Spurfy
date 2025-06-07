package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
public class Reservation {

    @Id
    private String reservationId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "dog_id")
    private Dog dog;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private SpaService spaService;

    private LocalDate reservationDate;
    private LocalTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false) // DB에 문자열로 저장하기!
    private ReservationStatus reservationStatus; // 1: 예약완료, 2: 취소됨 등
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus;      // 0: 없음, 1: 대기, 2: 완료

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    private String refundType;     // 자동 or 수동
    private String cancelReason;

    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

}
