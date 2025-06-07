package com.example.oyl.dto;

import lombok.*;
import org.hibernate.annotations.SecondaryRow;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private String reservationId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
}
