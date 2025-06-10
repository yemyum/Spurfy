package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
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
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("리뷰가 등록되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable String reviewId,
                                          @RequestBody ReviewUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.updateReview(reviewId, email, dto);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("리뷰가 수정되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.deleteReview(reviewId, email);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("리뷰가 삭제되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReviewMyPageDTO>>> getMyReviews() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ReviewMyPageDTO> reviews = reviewService.getMyReviews(email);
        return ResponseEntity.ok(
                ApiResponse.<List<ReviewMyPageDTO>>builder()
                        .code("S001")
                        .message("나의 리뷰 목록 조회 성공!")
                        .data(reviews)
                        .build()
        );
    }

    @GetMapping("/public/{serviceId}")
    public ResponseEntity<ApiResponse<List<ReviewPublicDTO>>> getReviewsByService(@PathVariable String serviceId) {
        List<ReviewPublicDTO> reviews = reviewService.getReviewsByService(serviceId);
        return ResponseEntity.ok(
                ApiResponse.<List<ReviewPublicDTO>>builder()
                        .code("S001")
                        .message("서비스별 리뷰 목록 조회 성공!")
                        .data(reviews)
                        .build()
        );
    }

}
