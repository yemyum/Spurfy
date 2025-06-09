package com.example.oyl.service;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.Review;
import com.example.oyl.domain.User;
import com.example.oyl.dto.ReviewMyPageDTO;
import com.example.oyl.dto.ReviewPublicDTO;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import com.example.oyl.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인 유저 없음"));
    }

    @Transactional
    @Override
    public void createReview(String userEmail, ReviewRequestDTO dto) {
        User user = getUserByEmail(userEmail);

        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약 없음"));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인의 예약이 아님");
        }

        if (reviewRepository.existsByReservation_ReservationId(dto.getReservationId())) {
            throw new RuntimeException("이미 작성된 리뷰가 있음");
        }

        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new RuntimeException("강아지 정보 없음"));

        Review review = Review.builder()
                .reviewId(UUID.randomUUID().toString())
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
        User user = getUserByEmail(userEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인이 작성한 리뷰만 수정 가능!");
        }

        review.updateReview(
                dto.getRating(),
                dto.getContent(),
                dto.getImageUrl(),
                LocalDateTime.now());
    }

    @Transactional
    @Override
    public void deleteReview(String reviewId, String userEmail) {
        User user = getUserByEmail(userEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("본인이 작성한 리뷰만 삭제할 수 있습니다!");
        }

        reviewRepository.delete(review);
    }

    @Override
    public List<ReviewMyPageDTO> getMyReviews(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Review> reviews = reviewRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return reviews.stream()
                .map(r -> ReviewMyPageDTO.builder()
                        .reviewId(r.getReviewId())
                        .reservationId(r.getReservation().getReservationId())
                        .serviceId(r.getReservation().getSpaService().getServiceId())
                        .serviceName(r.getReservation().getSpaService().getName())
                        .dogName(r.getDog().getName())
                        .reservationDate(r.getReservation().getReservationDate().toString())
                        .price(r.getReservation().getSpaService().getPrice())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .imageUrl(r.getImageUrl())
                        .createdAt(r.getCreatedAt().format(formatter))
                        .build())
                .toList();
    }

    @Override
    public List<ReviewPublicDTO> getReviewsByService(String serviceId) {
        List<Review> reviews = reviewRepository
                .findByReservationSpaServiceServiceIdAndIsBlindedFalseOrderByCreatedAtDesc(serviceId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return reviews.stream()
                .map(r -> ReviewPublicDTO.builder()
                        .reviewId(r.getReviewId())
                        .userNickname(r.getUser().getNickname())
                        .dogName(r.getDog().getName())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .imageUrl(r.getImageUrl())
                        .createdAt(r.getCreatedAt().format(formatter))
                        .build())
                .toList();
    }

}
