package com.example.oyl.repository;

import com.example.oyl.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {

    // 예약 Id로 리뷰 존재 여부 확인 (중복 방지용)
    boolean existsByReservation_ReservationId(String reservationId);

    // 마이페이지에서 로그인 유저의 리뷰 조회
    List<Review> findByUserUserIdOrderByCreatedAtDesc(String userId);

    // 특정 스파 서비스에 달린 모든 리뷰 조회 (비로그인 허용!)
    List<Review> findByReservationSpaServiceServiceIdAndIsBlindedFalseOrderByCreatedAtDesc(String serviceId);

}
