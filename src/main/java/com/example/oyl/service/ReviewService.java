package com.example.oyl.service;

import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;

public interface ReviewService {

    void createReview(String userEmail, ReviewRequestDTO dto);

    void updateReview(String reviewId, String userEmail, ReviewUpdateDTO dto);

    void deleteReview(String reviewId, String userEmail);

}
