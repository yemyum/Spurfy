package com.example.oyl.repository;

import com.example.oyl.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    // 예약 Id 기준으로 결제 정보 조회
    Optional<Payment> findFirstByReservation_ReservationId(String reservationId);

    boolean existsByReservation_ReservationId(String reservationId);

}
