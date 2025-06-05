package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
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

    private int reservationStatus; // 1: 예약완료, 2: 취소됨 등
    private int refundStatus;      // 0: 없음, 1: 대기, 2: 완료
    private String refundType;     // 자동 or 수동
    private String cancelReason;

    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}
