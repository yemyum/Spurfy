package com.example.oyl.service;

import com.example.oyl.dto.ReviewMyPageDTO;
import com.example.oyl.dto.ReviewPublicDTO;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    void createReview(String userEmail, ReviewRequestDTO dto);

    void updateReview(String reviewId, String userEmail, ReviewUpdateDTO dto);

    void deleteReview(String reviewId, String userEmail);

    List<ReviewMyPageDTO> getMyReviews(String userEmail);

    // List를 반환하는 메서드 대신, Page를 반환하는 메서드만 사용
    // List<ReviewPublicDTO> getReviewsByService(String serviceId);

    ReviewMyPageDTO getReviewDetailForMypage(String reviewId, String userEmail);

    // 페이징 처리를 위한 메서드.
    // 컨트롤러에서 호출될 메서드이고, 서비스 구현체에서 로직을 작성해야 함.
    Page<ReviewPublicDTO> getReviewsBySpaSlug(String spaSlug, Pageable pageable);

}