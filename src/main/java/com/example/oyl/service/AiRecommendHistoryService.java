package com.example.oyl.service;

import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.dto.AiRecommendHistoryResponseDTO;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendHistoryService {

    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;
    private final ObjectMapper objectMapper;

    public List<AiRecommendHistoryResponseDTO> getUserRecommendationHistory(String userId) {

        // 1. 레포지를 통해 DB에서 AiRecommendHistory 엔티티 리스트를 가져오기
        List<AiRecommendHistory> histories = aiRecommendHistoryRepository.findByUserId(userId);

        // 2. 필터링 없이 모든 엔티티를 DTO 리스트로 변환!
        // ✅ 모든 엔티티를 DTO로 변환하되, 에러가 있어도 DTO 자체는 생성하도록 수정!
        return histories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // AiRecommendHistory 엔티티 하나를 AiRecommendHistoryResponseDto 하나로 변환하는 도우미 메서드
    private AiRecommendHistoryResponseDTO convertToDTO(AiRecommendHistory history) {
        String intro = null, compliment = null, recommendationHeader = null, spaName = null, closing = null, spaSlug = null;
        List<String> spaDescription = new ArrayList<>();
        String errorMessage = history.getErrorMessage();

        // ✅ recommendResult가 있을 때만 JSON 파싱을 시도!
        if (history.getRecommendResult() != null && !history.getRecommendResult().trim().isEmpty()) {
        try {
            // JSON 문자열을 JsonNode 객체로 파싱
            JsonNode jsonNode = objectMapper.readTree(history.getRecommendResult());

            // 각 필드에 맞게 JSON 노드에서 값 추출
            intro = jsonNode.has("intro") ? jsonNode.get("intro").asText() : null;
            compliment = jsonNode.has("compliment") ? jsonNode.get("compliment").asText() : null;
            recommendationHeader = jsonNode.has("recommendationHeader") ? jsonNode.get("recommendationHeader").asText() : null;
            spaName = jsonNode.has("spaName") ? jsonNode.get("spaName").asText() : null;
            closing = jsonNode.has("closing") ? jsonNode.get("closing").asText() : null;
            spaSlug = jsonNode.has("spaSlug") ? jsonNode.get("spaSlug").asText() : null;

            // spaDescription은 배열일 수 있으니 특별히 처리
            if (jsonNode.has("spaDescription") && jsonNode.get("spaDescription").isArray()) {
                for (JsonNode descNode : jsonNode.get("spaDescription")) {
                    spaDescription.add(descNode.asText());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse recommendResult JSON for history id: {}", history.getId(), e);
            // ✅ 파싱에 실패해도 DTO는 생성되도록 여기서 null을 리턴하지 않음
        }
        }

        // Builder 패턴을 사용해서 DTO 객체 생성
        return AiRecommendHistoryResponseDTO.builder()
                .id(history.getId())
                .imageUrl(history.getImageUrl())
                .detectedBreed(history.getDetectedBreed())
                .prompt(history.getPrompt())
                .createdAt(history.getCreatedAt())
                .intro(intro)
                .compliment(compliment)
                .recommendationHeader(recommendationHeader)
                .spaName(spaName)
                .spaDescription(spaDescription)
                .closing(closing)
                .spaSlug(spaSlug)
                .errorMessage(errorMessage)
                .build();
    }
}
