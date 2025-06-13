package com.example.oyl.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationPaymentRequestDTO {
    private String dogId;
    private String serviceId;
    private String reservationDate; // yyyy-MM-dd
    private String reservationTime; // HH:mm
    private String paymentMethod; // 예: "CARD"
    private int amount;
}
