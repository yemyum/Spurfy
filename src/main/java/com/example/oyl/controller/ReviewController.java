package com.example.oyl.controller;

import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import com.example.oyl.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDTO dto,
                                          @AuthenticationPrincipal String userEmail) {

        reviewService.createReview(userEmail, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable String reviewId,
                                          @RequestBody ReviewUpdateDTO dto,
                                          @AuthenticationPrincipal String userEmail) {
        reviewService.updateReview(reviewId, userEmail, dto);
        return ResponseEntity.ok().build(); // or 메시지 포함도 가능!
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId,
                                          @AuthenticationPrincipal String userEmail) {
        reviewService.deleteReview(reviewId, userEmail);
        return ResponseEntity.ok().build(); // or 메시지 포함 가능!
    }

}
