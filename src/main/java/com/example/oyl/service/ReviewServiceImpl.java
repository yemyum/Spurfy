package com.example.oyl.service;

import com.example.oyl.domain.*;
import com.example.oyl.dto.ReviewMyPageDTO;
import com.example.oyl.dto.ReviewPublicDTO;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    @Override
    public void createReview(String userEmail, ReviewRequestDTO dto) {

        // 1. 유저 조회
        User user = getUserByEmail(userEmail);

        // 2. 예약 조회
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.NOT_COMPLETED_RESERVATION);
        }


        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
        }

        if (reviewRepository.existsByReservation_ReservationId(dto.getReservationId())) {
            throw new CustomException(ErrorCode.DUPLICATE_REVIEW);
        }

        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
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
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewMyPageDTO> getMyReviews(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Review> reviews = reviewRepository.findAllWithDogAndServiceByUserId(user.getUserId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return reviews.stream()
                .map(r -> {
                    String spaSlug = null;
                    if (r.getReservation() != null && r.getReservation().getSpaService() != null) {
                        spaSlug = r.getReservation().getSpaService().getSlug();
                    }

                    return ReviewMyPageDTO.builder()
                            .reviewId(r.getReviewId())
                            .reservationId(r.getReservation() != null ? r.getReservation().getReservationId() : null)
                            .serviceId(r.getReservation() != null && r.getReservation().getSpaService() != null ? r.getReservation().getSpaService().getServiceId() : null)
                            .serviceName(r.getReservation() != null && r.getReservation().getSpaService() != null ? r.getReservation().getSpaService().getName() : null)
                            .dogName(r.getDog() != null ? r.getDog().getName() : null)
                            .reservationDate(r.getReservation() != null && r.getReservation().getReservationDate() != null ? r.getReservation().getReservationDate().toString() : null)
                            .price(r.getReservation() != null && r.getReservation().getSpaService() != null ? r.getReservation().getSpaService().getPrice() : null)
                            .rating(r.getRating())
                            .content(r.getContent())
                            .imageUrl(r.getImageUrl())
                            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().format(formatter) : null)
                            .isBlinded(r.isBlinded())
                            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().format(formatter) : null)
                            .spaSlug(spaSlug)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewPublicDTO> getReviewsByService(String serviceId) {
        List<Review> reviews = reviewRepository.findAllWithUserAndDogByServiceId(serviceId);

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

    @Override
    @Transactional(readOnly = true)
    public ReviewMyPageDTO getReviewDetailForMypage(String reviewId, String userEmail) {

        // 1. 현재 로그인한 사용자 정보 조회
        User currentUser = getUserByEmail(userEmail);

        // 2. reviewId로 리뷰 엔티티 조회 (리뷰가 없으면 예외 발생)
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 3. 권한 체크 : 조회하려는 리뷰의 작성자가 현재 로그인한 사용자와 동일한지 확인!
        // 만약 리뷰의 user ID가 현재 로그인한 user ID와 다르면 접근 권한 없음 예외 발생
        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS); // 권한 없음
        }

            // 4. 리뷰 상세 정보를 DTO로 반환
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            return ReviewMyPageDTO.builder()
                    .reviewId(review.getReviewId())
                    .reservationId(review.getReservation().getReservationId())
                    .serviceId(review.getReservation().getSpaService().getServiceId())
                    .serviceName(review.getReservation().getSpaService().getName())
                    .dogName(review.getDog().getName())
                    .reservationDate(review.getReservation().getReservationDate().toString()) // ReservationDate가 LocalDate면 toString()으로 충분
                    .price(review.getReservation().getSpaService().getPrice())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .imageUrl(review.getImageUrl())
                    .createdAt(review.getCreatedAt().format(formatter))
                    .isBlinded(review.isBlinded()) // boolean 타입 그대로
                    .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().format(formatter) : null) // updatedAt이 null일 수 있으니 체크
                    .build();
    }

    public List<ReviewPublicDTO> getReviewsBySpaSlug(String spaSlug) {
        // 1. ReviewRepository를 사용해서 해당 슬러그를 가진 스파의 리뷰들을 가져옴
        List<Review> reviews = reviewRepository.findBySpaServiceSlug(spaSlug);

        // 2. 가져온 Review 엔티티들을 ReviewPublicDTO로 변환해서 반환
        return reviews.stream()
                .map(r -> ReviewPublicDTO.builder()
                        .reviewId(r.getReviewId())
                        .userNickname(r.getUser() != null ? r.getUser().getNickname() : "알 수 없음") // User에서 닉네임 가져옴
                        .content(r.getContent())
                        .rating(r.getRating())
                        .imageUrl(r.getImageUrl())
                        .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
                        .build())
                .collect(Collectors.toList());
    }


}
