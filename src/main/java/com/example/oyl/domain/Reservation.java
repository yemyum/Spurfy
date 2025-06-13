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

    //!! 여기서 Enum 다 빼고 String으로!!
    @Column(name = "reservation_status", nullable = false)
    private String reservationStatus;

    @Column(name = "refund_status", nullable = false)
    private String refundStatus;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    private String refundType;
    private String cancelReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;

    // setter도 String으로 고쳐!
    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
}
