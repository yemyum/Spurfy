package com.example.oyl.service;

import com.example.oyl.dto.ReviewMyPageDTO;
import com.example.oyl.dto.ReviewPublicDTO;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;

import java.util.List;

public interface ReviewService {

    void createReview(String userEmail, ReviewRequestDTO dto);

    void updateReview(String reviewId, String userEmail, ReviewUpdateDTO dto);

    void deleteReview(String reviewId, String userEmail);

    List<ReviewMyPageDTO> getMyReviews(String userEmail);

    List<ReviewPublicDTO> getReviewsByService(String serviceId);

    ReviewMyPageDTO getReviewDetailForMypage(String reviewId, String userEmail);

    List<ReviewPublicDTO> getReviewsBySpaSlug(String spaSlug);

}
