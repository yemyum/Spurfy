package com.example.oyl.dto;

import com.example.oyl.domain.*;
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
    private ReservationStatus reservationStatus;
    private Long price;
    private Long amount;           // 실제 결제 금액
    private String paymentMethod;  // 결제 수단]
    private RefundStatus refundStatus;
    private RefundType refundType;
    private String cancelReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private boolean hasReview;

    public static ReservationResponseDTO from(Reservation reservation, boolean hasReview) {
        Payment payment = reservation.getPayment();

        return ReservationResponseDTO.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUser().getUserId())
                .dogId(reservation.getDog().getDogId())
                .serviceId(reservation.getSpaService().getServiceId())
                .dogName(reservation.getDog().getName())
                .serviceName(reservation.getSpaService().getName())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .reservationStatus(reservation.getReservationStatus())
                .price(reservation.getPrice())
                .amount(payment != null ? payment.getAmount().longValue() : reservation.getPrice())
                .paymentMethod(payment != null ? payment.getPaymentMethod().toString() : null)
                .refundStatus(reservation.getRefundStatus())
                .refundType(reservation.getRefundType())
                .cancelReason(reservation.getCancelReason())
                .refundedAt(reservation.getRefundedAt())
                .createdAt(reservation.getCreatedAt())
                .hasReview(hasReview)
                .build();
    }
}
