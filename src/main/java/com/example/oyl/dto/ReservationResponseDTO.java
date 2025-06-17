package com.example.oyl.dto;

import com.example.oyl.domain.RefundStatus;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class ReservationResponseDTO {
    private String reservationId;
    private String userId;
    private String dogId;
    private String serviceId;
    private String dogName;
    private String serviceName;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private String reservationStatus; // <== String!
    private String refundStatus;      // <== String!
    private String refundType;
    private String cancelReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;

    // from 메소드
    public static ReservationResponseDTO from(Reservation reservation) {
        return ReservationResponseDTO.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUser().getUserId())
                .dogId(reservation.getDog().getDogId())
                .serviceId(reservation.getSpaService().getServiceId())
                .dogName(reservation.getDog().getName())
                .serviceName(reservation.getSpaService().getName())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .reservationStatus(reservation.getReservationStatus()) // 그대로!
                .refundStatus(reservation.getRefundStatus()) // 그대로!
                .refundType(reservation.getRefundType())
                .cancelReason(reservation.getCancelReason())
                .refundedAt(reservation.getRefundedAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
