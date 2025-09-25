package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.ReviewMyPageDTO;
import com.example.oyl.dto.ReviewPublicDTO;
import com.example.oyl.dto.ReviewRequestDTO;
import com.example.oyl.dto.ReviewUpdateDTO;
import com.example.oyl.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewMyPageDTO>> getReviewDetail(@PathVariable String reviewId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 현재 로그인한 사용자 이메일

        ReviewMyPageDTO reviewDetail = reviewService.getReviewDetailForMypage(reviewId, userEmail);

        return ResponseEntity.ok(
                ApiResponse.<ReviewMyPageDTO>builder()
                        .code("S001")
                        .message("리뷰 상세 조회 성공!")
                        .data(reviewDetail)
                        .build()
        );
    }

    @GetMapping("/public/slug/{spaSlug}")
    public ResponseEntity<ApiResponse<Page<ReviewPublicDTO>>> getReviewsBySpaSlug(
            @PathVariable String spaSlug,
            @PageableDefault(page = 0, size = 5) Pageable pageable) {

        // 서비스 메서드에 pageable 객체 전달
        Page<ReviewPublicDTO> reviews = reviewService.getReviewsBySpaSlug(spaSlug, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<ReviewPublicDTO>>builder()
                        .code("S001")
                        .message("슬러그 기반 스파 리뷰 목록 조회 성공!")
                        .data(reviews)
                        .build()
        );
    }

}
