package com.example.oyl.repository;

import com.example.oyl.domain.Review;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {

    // 예약 Id로 리뷰 존재 여부 확인 (중복 방지용)
    boolean existsByReservation_ReservationId(String reservationId);

    // 리뷰와 연결된 유저, 강아지를 한 번에 전부 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.user u JOIN FETCH r.dog d WHERE r.reservation.spaService.serviceId = :serviceId AND r.isBlinded = false ORDER BY r.createdAt DESC")
    List<Review> findAllWithUserAndDogByServiceId(@Param("serviceId") String serviceId);

    @Query("SELECT r FROM Review r JOIN FETCH r.dog d JOIN FETCH r.reservation res JOIN FETCH res.spaService WHERE r.user.userId = :userId ORDER BY r.createdAt DESC")
    List<Review> findAllWithDogAndServiceByUserId(@Param("userId") String userId);

    @Query("SELECT r FROM Review r JOIN FETCH r.reservation res JOIN FETCH res.spaService ss JOIN FETCH r.user u JOIN FETCH r.dog d WHERE ss.slug = :spaSlug ORDER BY r.createdAt DESC")
    Page<Review> findBySpaServiceSlug(@Param("spaSlug") String spaSlug, Pageable pageable); // Pageable 파라미터 추가
}
