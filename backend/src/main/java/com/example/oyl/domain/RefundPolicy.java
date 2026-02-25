package com.example.oyl.domain;

import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class RefundPolicy { // 추후 "정책이 여러 개(VIP, 이벤트, 블랙리스트, 계약 고객)"인 경우 인터페이스로 설계할 것

    public RefundResult calculate(Reservation reservation, Payment payment) {

        LocalDateTime reservationDateTime =
                LocalDateTime.of(reservation.getReservationDate(),
                        reservation.getReservationTime());

        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservationDateTime);

        if (hours >= 72) {
            return new RefundResult(RefundType.FULL, payment.getAmount());
        } else if (hours >= 24) {
            BigDecimal partial = payment.getAmount()
                    .multiply(BigDecimal.valueOf(0.5));
            return new RefundResult(RefundType.PARTIAL, partial);
        } else {
            throw new CustomException(ErrorCode.REFUND_POLICY_VIOLATION);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class RefundResult {   // 내부 static
        private RefundType refundType;
        private BigDecimal refundAmount;
    }

}
