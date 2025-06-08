package com.example.oyl.repository;

import com.example.oyl.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, String> {

    // 예약 Id로 리뷰 존재 여부 확인 (중복 방지용)
    boolean existsByReservation_ReservationId(String reservationId);
}
