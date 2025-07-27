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

        // 2. 가져온 엔티티 리스트를 DTO 리스트로 변환!
        return histories.stream()
                .filter(history -> history.getRecommendResult() != null && !history.getRecommendResult().trim().isEmpty())
                .map(this::convertToDTO)
                .filter(Objects::nonNull)
                .filter(dto -> dto.getIntro() != null || dto.getSpaName() != null)
                .collect(Collectors.toList());
    }

    // AiRecommendHistory 엔티티 하나를 AiRecommendHistoryResponseDto 하나로 변환하는 도우미 메서드
    private AiRecommendHistoryResponseDTO convertToDTO(AiRecommendHistory history) {
        // recommendResult는 JSON 문자열이니까 파싱해야 해!
        String intro = null;
        String compliment = null;
        String recommendationHeader = null;
        String spaName = null;
        List<String> spaDescription = new ArrayList<>();
        String closing = null;
        String spaSlug = null;

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
            // 에러 발생 시 DTO 필드를 null 또는 기본값으로 남겨두거나, 에러 DTO를 반환하는 등 처리할 수 있음
        }

        // Builder 패턴을 사용해서 DTO 객체 생성
        return AiRecommendHistoryResponseDTO.builder()
                .id(history.getId())
                .imageUrl(history.getImageUrl())
                .detectedBreed(history.getDetectedBreed())
                .prompt(history.getPrompt())
                .createdAt(history.getCreatedAt())
                // JSON 파싱 결과
                .intro(intro)
                .compliment(compliment)
                .recommendationHeader(recommendationHeader)
                .spaName(spaName)
                .spaDescription(spaDescription)
                .closing(closing)
                .spaSlug(spaSlug)
                .build();
    }
}
