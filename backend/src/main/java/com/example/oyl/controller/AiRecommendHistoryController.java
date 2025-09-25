package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.AiRecommendHistoryResponseDTO;
import com.example.oyl.service.AiRecommendHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class AiRecommendHistoryController {

    private final AiRecommendHistoryService aiRecommendHistoryService;

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AiRecommendHistoryResponseDTO>>> getUserRecommendationHistory() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        List<AiRecommendHistoryResponseDTO> historyList = aiRecommendHistoryService.getUserRecommendationHistory(userEmail);

        return ResponseEntity.ok(
                ApiResponse.<List<AiRecommendHistoryResponseDTO>>builder()
                        .code("S001")
                        .message("AI 추천 기록 조회 성공!")
                        .data(historyList)
                        .build()
        );

    }
}
