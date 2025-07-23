package com.example.oyl.service;

import com.example.oyl.client.GoogleVisionClient;
import com.example.oyl.client.GptClient;
import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.dto.SpaLabelRecommendationRequestDTO;
import com.example.oyl.dto.SpaRecommendationRequestDTO;
import com.example.oyl.dto.VisionAnalysisResult;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DogImageService {

    private final GoogleVisionClient googleVisionClient;
    private final GptClient gptClient;
    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;

    private static final int MAX_DAILY_AI_CALLS = 10;

    public String analyzeAndRecommendSpa(MultipartFile dogImageFile, String userEmail, String question) {

        if (dogImageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);

        long todayCount = aiRecommendHistoryRepository.countByUserIdAndCreatedAtBetween(userEmail, startOfDay, endOfDay);

        if (todayCount >= MAX_DAILY_AI_CALLS) {
            log.warn("AI 대화 횟수 제한 초과! 현재 호출 횟수: {}, 최대 허용: {}", todayCount, MAX_DAILY_AI_CALLS);
            throw new CustomException(ErrorCode.CONVERSATION_LIMIT_EXCEEDED,
                    "하루 AI 대화 횟수(" + MAX_DAILY_AI_CALLS + "회)를 초과했습니다. 내일 다시 시도해주세요.");
        }

        String uploadDir = "uploads";
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String savedFileName;

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = dogImageFile.getOriginalFilename();
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');

            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
                originalFileName = originalFileName.substring(0, dotIndex);
            }

            savedFileName = originalFileName + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS")) + fileExtension;

            Path filePath = uploadPath.resolve(savedFileName);
            Files.copy(dogImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("이미지 저장 완료 → {}", filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "파일 저장 중 오류 발생: " + e.getMessage());
        }

        VisionAnalysisResult visionResult;
        String detectedBreed;
        List<String> labels;

        try {
            visionResult = googleVisionClient.analyzeImage(dogImageFile);

            if (!visionResult.isDog()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "사진 속 강아지를 찾아볼 수 없어요. 강아지 사진을 다시 올려주세요!");
            }

            detectedBreed = visionResult.getDetectedBreed();

            // 1. 필터링 키워드(무시할 라벨) 미리 선언
            List<String> bannedLabels = List.of("clothes", "costume", "pet supply", "clothing", "supply");

            // 2. 필터링해서 라벨만 추출!
            labels = visionResult.getLabels().stream()
                    .map(label -> label.getDescription())
                    .filter(desc -> bannedLabels.stream().noneMatch(bad -> desc.toLowerCase().contains(bad)))
                    .toList();

        } catch (CustomException e) {
            log.warn("Vision 분석 실패 → {}", e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "견종 분석 실패: " + e.getMessage());
        }

        log.info("Vision 분석 완료 → {}", detectedBreed);

        String spaRecommendation;
        try {
            if ("알 수 없는 견종의 강아지".equals(detectedBreed)) {
                // 라벨 기반 추천으로 fallback!
                SpaLabelRecommendationRequestDTO labelDto = SpaLabelRecommendationRequestDTO.builder()
                        .labels(labels)
                        .ageGroup("성견")
                        .skinTypes(List.of())
                        .healthConditions(List.of())
                        .activityLevel("활발함")
                        .build();

                spaRecommendation = gptClient.recommendSpaByLabels(labelDto);
            } else {
                SpaRecommendationRequestDTO request = new SpaRecommendationRequestDTO(
                        detectedBreed,
                        "성견",
                        List.of(),
                        List.of(),
                        "활발함"
                );
                spaRecommendation = gptClient.recommendSpa(request);
            }
        } catch (Exception e) {
            log.error("GPT 호출 실패", e);
            throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED, "스파 추천 실패: " + e.getMessage());
        }

        log.info("GPT 추천 완료 → {}", spaRecommendation);

        try {
            String imageUrlForHistory = "/api/images/" + savedFileName;

            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrlForHistory)
                    .detectedBreed(detectedBreed)
                    .isDog(true)
                    .recommendResult(spaRecommendation)
                    .prompt(question)
                    .errorMessage(null)
                    .build();

            aiRecommendHistoryRepository.save(history);
            log.info("AI 추천 기록 DB 저장 완료 → user={}, breed={}", userEmail, detectedBreed);
        } catch (Exception e) {
            log.warn("AI 추천 기록 저장 실패 → {}", e.getMessage());
        }

        String imageUrl = "/api/images/" + savedFileName;
        if ("알 수 없는 견종의 강아지".equals(detectedBreed)) {
            return spaRecommendation;  // 그냥 추천 멘트만 출력
        } else {
            return "견종: " + detectedBreed + ", 추천 스파: " + spaRecommendation;
        }
    }
}