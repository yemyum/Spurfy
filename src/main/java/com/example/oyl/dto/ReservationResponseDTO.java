package com.example.oyl.dto;

import com.example.oyl.domain.RefundStatus;
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
    private ReservationStatus reservationStatus;
    private RefundStatus refundStatus;
    private String refundType;
    private String cancelReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}
