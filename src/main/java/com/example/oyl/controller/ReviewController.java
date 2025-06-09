package com.example.oyl.controller;

import com.example.oyl.dto.ReviewMyPageDTO;
import com.example.oyl.dto.ReviewPublicDTO;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import com.example.oyl.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;


    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.createReview(email, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable String reviewId,
                                          @RequestBody ReviewUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.updateReview(reviewId, email, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.deleteReview(reviewId, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewMyPageDTO>> getMyReviews() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ReviewMyPageDTO> reviews = reviewService.getMyReviews(email);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/public/{serviceId}")
    public ResponseEntity<List<ReviewPublicDTO>> getReviewsByService(@PathVariable String serviceId) {
        List<ReviewPublicDTO> reviews = reviewService.getReviewsByService(serviceId);
        return ResponseEntity.ok(reviews);
    }

}
