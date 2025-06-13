package com.example.oyl.dto;

import com.example.oyl.domain.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String paymentId;
    private String reservationId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;

    public static PaymentResponseDTO from(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .reservationId(payment.getReservation().getReservationId())
                .userId(payment.getUser().getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
