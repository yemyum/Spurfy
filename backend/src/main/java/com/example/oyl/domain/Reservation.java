package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "reservation_id", length = 36)
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

    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private Payment payment;

    private LocalDate reservationDate;
    private LocalTime reservationTime;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type")
    private RefundType refundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status")
    private RefundStatus refundStatus;

    private String cancelReason;

    private LocalDateTime refundedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private Review review;

    public void setReservationStatus(ReservationStatus reservationStatus) {this.reservationStatus = reservationStatus;}

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
}
