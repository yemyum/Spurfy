package com.example.oyl.service;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.Review;
import com.example.oyl.domain.User;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import com.example.oyl.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;

    @Override
    public void createReview(String userEmail, ReviewRequestDTO dto) {
        // 1. 유저 검증
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));

        // 2. 예약 검증
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약 없음"));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인의 예약이 아님");
        }

        // 3. 중복 리뷰 방지
        if (reviewRepository.existsByReservation_ReservationId(dto.getReservationId())) {
            throw new RuntimeException("이미 작성된 리뷰가 있음");
        }

        // 4. 강아지 정보 가져오기
        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new RuntimeException("강아지 정보 없음"));

        // 5. 리뷰 생성
        Review review = Review.builder()
                .reviewId((UUID.randomUUID().toString()))
                .reservation(reservation)
                .user(user)
                .dog(dog)
                .rating(dto.getRating())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .isBlinded(false)
                .createdAt(LocalDateTime.now())
                .build();


        reviewRepository.save(review);


    }

    @Transactional
    @Override
    public void updateReview(String reviewId, String userEmail, ReviewUpdateDTO dto) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 2. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        // 3. 작성자 검증
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인이 작성한 리뷰만 수정 가능!");
        }

        // 4. 값 변경
        review.updateReview(
                dto.getRating(),
                dto.getContent(),
                dto.getImageUrl(),
                LocalDateTime.now());
    }

    @Transactional
    @Override
    public void deleteReview(String reviewId, String userEmail) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 2. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        // 3. 작성자 검증
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인이 작성한 리뷰만 삭제할 수 있습니다!");
        }

        // 4. 삭제
        reviewRepository.delete(review);
    }


}
