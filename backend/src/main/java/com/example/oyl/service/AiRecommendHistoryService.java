package com.example.oyl.service;

import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.dto.AiRecommendHistoryResponseDTO;
import com.example.oyl.dto.GptSpaRecommendationResponseDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendHistoryService {

    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;
    private final ObjectMapper objectMapper;

    public List<AiRecommendHistoryResponseDTO> getUserRecommendationHistory(String userId) {

        int historyRetentionDays = 7; // 기준일을 상수로 빼두기
        LocalDateTime fetchLimitPeriod = LocalDateTime.now().minusDays(historyRetentionDays);

        List<AiRecommendHistory> histories = aiRecommendHistoryRepository.findByUserIdAndCreatedAtAfter(userId, fetchLimitPeriod);

        return histories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 성공 엔티티 리턴 버전 (메인 서비스에서 id랑 날짜 세팅할 수 있게)
    public AiRecommendHistory saveSuccess(String userEmail, String imageUrl, String detectedBreed,
                                          boolean isDog, String recommendResultJson, String question) {
        try {
            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrl)
                    .detectedBreed(detectedBreed)
                    .isDog(isDog)
                    .recommendResult(recommendResultJson)
                    .prompt(question)
                    .errorMessage(null)
                    .build();

            return aiRecommendHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("AI 추천 기록 DB 저장 중 예외 발생: ", e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "추천 기록 저장에 실패했습니다.");
            // 💡 null 대신 예외를 던져 서비스 트랜잭션을 안전하게 지키거나 비즈니스 상황에 맞춰 선택!
        }
    }

    // 실패 이력 저장 전담
    public void saveFailureHistory(String userEmail, String imageUrl, String detectedBreed, String errorMessage, String question) {
        try {
            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrl)
                    .detectedBreed(detectedBreed)
                    .isDog(false)
                    .recommendResult(null)
                    .prompt(question)
                    .errorMessage(errorMessage)
                    .build();

            aiRecommendHistoryRepository.save(history);
            log.info("AI 추천 기록 저장 완료 (실패) → user={}, reason={}", userEmail, errorMessage);
        } catch (Exception e) {
            log.error("AI 추천 실패 기록 DB 저장 중 예외 발생: ", e);
        }
    }

    // AiRecommendHistory 엔티티 하나를 AiRecommendHistoryResponseDto 하나로 변환하는 도우미 메서드
    private AiRecommendHistoryResponseDTO convertToDTO(AiRecommendHistory history) {
        // 1. 변수들을 일단 null로 만들어두기
        String intro = null, compliment = null, recommendationHeader = null, spaName = null, closing = null, spaSlug = null;
        List<String> spaDescription = new ArrayList<>();
        String errorMessage = history.getErrorMessage();

        if (history.getRecommendResult() != null && !history.getRecommendResult().trim().isEmpty()) {
            try {
                // 🌟 통째로 한 번에 구워내기!
                GptSpaRecommendationResponseDTO parsedResult = objectMapper.readValue(
                        history.getRecommendResult(), GptSpaRecommendationResponseDTO.class
                );

                // 🌟 바구니에 이쁘게 담긴 놈들을 그냥 쏙쏙 꺼내 쓰기만 하면 끝!
                intro = parsedResult.getIntro();
                compliment = parsedResult.getCompliment();
                recommendationHeader = parsedResult.getRecommendationHeader();
                spaName = parsedResult.getSpaName();
                closing = parsedResult.getClosing();
                spaSlug = parsedResult.getSpaSlug();

                // 리스트도 null 체크만 가볍게 해주고 통째로 넣어주면 끝!
                if (parsedResult.getSpaDescription() != null) {
                    spaDescription = parsedResult.getSpaDescription();
                }

            } catch (Exception e) {
                log.error("[History] JSON 파싱 에러 (ID: {})", history.getId(), e);
            }
        }

        // 2. 마지막에 조립해서 리턴
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
