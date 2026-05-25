package com.example.oyl.service;

import com.example.oyl.client.GoogleVisionClient;
import com.example.oyl.client.GptClient;
import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.dto.GptSpaRecommendationResponseDTO;
import com.example.oyl.dto.SpaLabelRecommendationRequestDTO;
import com.example.oyl.dto.SpaRecommendationRequestDTO;
import com.example.oyl.dto.VisionAnalysisResult;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import com.example.oyl.repository.SpaServiceRepository;
import com.example.oyl.util.ChecklistParser;
import com.example.oyl.util.ChecklistResult;
import com.example.oyl.util.ImageStorageUtil;
import com.example.oyl.util.TextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final GoogleVisionClient googleVisionClient;
    private final GptClient gptClient;
    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;
    private final AiRecommendHistoryService aiRecommendHistoryService;
    private final SpaServiceRepository spaServiceRepository;
    private final ObjectMapper objectMapper;
    private final ImageStorageUtil imageStorageUtil;
    private final RecommendationProcessor recommendationProcessor;
    private final AIRecommendationValidator aiRecommendationValidator;
    private final VisionResultAnalyzer visionResultAnalyzer;

    public int getTodayAiCallCount(String userEmail) {
        return aiRecommendationValidator.getTodayAiCallCount(userEmail);
    }

    // 대화 횟수 감지 + 이미지 저장 -> vision 사진 분석 -> GPT 추천
    public GptSpaRecommendationResponseDTO analyzeAndRecommendSpa(MultipartFile dogImageFile, String userEmail, String checklist, String question) {

        // 💡 검증기한테 "이 유저 오늘 한도 초과인지 검사해줘!"
        aiRecommendationValidator.validateDailyLimit(userEmail);

        // 필수 파라미터 체크
        if (dogImageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // ✅ 파일 저장
        final String imageUrlForHistory = imageStorageUtil.saveAndGetWebUrl(dogImageFile);

        String detectedBreed;
        List<String> visionLabels;
        boolean visionBreedUsable;
        boolean labelsUsable;

        try {
            // 구글 비전 API 호출
            VisionAnalysisResult visionResult = googleVisionClient.analyzeImage(dogImageFile);

            // 💡 [핵심] 복잡한 마릿수 체크, 금지어 필터링, 예외 처리를 분석기에게 통째로 위임!
            VisionResultAnalyzer.VisionAnalyzeResult visionAnalysis = visionResultAnalyzer.analyze(visionResult);

            // 분석기 바구니에서 필요한 결과값들 쏙쏙 꺼내오기
            detectedBreed = visionAnalysis.detectedBreed();
            visionLabels = visionAnalysis.visionLabels();
            visionBreedUsable = visionAnalysis.visionBreedUsable();
            labelsUsable = visionAnalysis.labelsUsable();

        } catch (CustomException e) {
            log.warn("Vision 분석 실패 → {}", e.getMessage());

            // 💡 실패 시 상단에서 설정되지 못한 기본값 임시 세팅
            detectedBreed = "알 수 없는 견종";

            // 실패 기록 저장
            aiRecommendHistoryService.saveFailureHistory(userEmail, imageUrlForHistory, detectedBreed, e.getMessage(), question);

            return GptSpaRecommendationResponseDTO.createFailureResponse(e.getMessage(), imageUrlForHistory);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "견종 분석 실패: " + e.getMessage());
        }

        log.info("Vision 분석 완료 → breed={}, labels={}", detectedBreed, visionLabels);


        // ✅ 복잡한 체크리스트 파싱, 데이터 정제를 프로세서로 위임!
        RecommendationProcessor.RefinedGptInputData inputData =
                recommendationProcessor.refineInputData(checklist, detectedBreed);

        // ✅ 검증기(Analyzer)를 불러서 유효한 견종인지 판단!
        boolean hasUsableBreed = !visionResultAnalyzer.isUnknownBreed(inputData.breedForPrompt());

        // GPT 호출 전에 지금 뭐 들고 있는지 최종 확인 로그
        log.info("[GPT-IN] breed='{}'(usable={}), age='{}', act='{}', issues={}",
                inputData.breedForPrompt(), hasUsableBreed, inputData.finalAgeGroup(), inputData.finalAdjActivity(), inputData.finalHealthIssues());

        // AI가 견종도 모르겠고, 다른 강아지 관련 단서(라벨)도 못 찾았을 때 : "추천을 해줄 만큼 정보가 충분한지?"
        if (!hasUsableBreed && !labelsUsable) {
            log.info("GPT 차단: breed/labels 부적합 → 안내만 반환");

            // 실패 기록 저장
            aiRecommendHistoryService.saveFailureHistory(
                    userEmail, imageUrlForHistory, detectedBreed,
                    "사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!", question
            );

            return GptSpaRecommendationResponseDTO.createFailureResponse(
                    "사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!", imageUrlForHistory
            );
        }

        // ✅ 결정된 값들을 바탕으로 GPT API를 호출하고 응답을 처리하는 로직 시작
        GptSpaRecommendationResponseDTO spaRecommendationDto;
        try {

            // 로그: inputData 바구니에서 값을 꺼내오도록
            log.info("[GPT-IN] breed='{}'(usable={}), age='{}', act='{}', issuesCnt={}",
                    inputData.breedForPrompt(), hasUsableBreed, inputData.finalAgeGroup(), inputData.finalAdjActivity(),
                    inputData.finalHealthIssues() != null ? inputData.finalHealthIssues().size() : 0);

            // Vision API 결과에 따라 다른 GPT 클라이언트를 호출
            if (!visionBreedUsable) { // Vision이 견종 모름 → 라벨 기반 GPT 호출
                log.info("Calling gptClient.recommendSpaByLabels...");

                // Vision도 모름 + 라벨 단서도 없음 -> 실패 처리
                if (!labelsUsable) {
                    // 💡 [리팩토링 1] 빌더로 직접 구워 저장하던 걸 전담 서비스 호출로 한 줄 컷!
                    aiRecommendHistoryService.saveFailureHistory(
                            userEmail, imageUrlForHistory, detectedBreed,
                            "사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!", question
                    );

                    return GptSpaRecommendationResponseDTO.createFailureResponse(
                            "사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!", imageUrlForHistory
                    );
                }

                SpaLabelRecommendationRequestDTO labelDto = SpaLabelRecommendationRequestDTO.builder()
                        .labels(Optional.ofNullable(visionLabels).orElse(List.of()))
                        .ageGroup(inputData.finalAgeGroup())
                        .skinTypes(List.of())
                        .healthIssues(inputData.finalHealthIssues())
                        .activityLevel(inputData.finalAdjActivity())
                        .checklist(checklist)
                        .question(question)
                        .breed(detectedBreed)
                        .selectedBreed(inputData.userSelectedBreed())  // 보호자 선택
                        .build();

                spaRecommendationDto = gptClient.recommendSpaByLabels(labelDto);

            } else {  // Vision이 견종 확정 → 견종 기반 GPT 호출
                log.info("Calling gptClient.recommendSpa...");
                SpaRecommendationRequestDTO request = SpaRecommendationRequestDTO.builder()
                        .breed(detectedBreed)
                        .ageGroup(inputData.finalAgeGroup())
                        .skinTypes(List.of())
                        .healthIssues(inputData.finalHealthIssues())
                        .activityLevel(inputData.finalAdjActivity())
                        .checklist(checklist)
                        .question(question)
                        .build();

                spaRecommendationDto = gptClient.recommendSpa(request);
            }

            // 3) 널 가드 (GPT가 응답을 안 준 경우)
            if (spaRecommendationDto == null) {
                throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED,
                        "AI 추천 결과를 가져오지 못했어요. 잠시 후 다시 시도해주세요.");
            }

            // 4) spaSlug 보정 (GPT가 slug를 안줬다면 DB에서 찾아 채움)
            if (spaRecommendationDto.getSpaSlug() == null && spaRecommendationDto.getSpaName() != null) {
                String cleanSpaName = TextUtils.normalizeSpaName(spaRecommendationDto.getSpaName());
                spaServiceRepository.findByName(cleanSpaName).ifPresent(spa ->
                        spaRecommendationDto.setSpaSlug(spa.getSlug())
                );
            }

            // 5) 출력 문구 후처리 (중복 수식어 정리)
            recommendationProcessor.cleanUpResponseText(spaRecommendationDto);

        } catch (CustomException e) {
            throw e; // 의도적으로 발생시킨 널 가드 에러는 그대로 통과!
        } catch (Exception e) {
            log.error("예상치 못한 GPT 호출 실패", e);
            throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED,
                    "AI 서비스가 현재 불안정합니다. 잠시 후 다시 시도해 주세요.");
        }

        // 예외 없이 try문이 끝났을 때만 실행
        log.info("GPT 추천 DTO 완료 → {}", spaRecommendationDto);

        // ✅ AI 추천 기록을 DB에 저장하는 로직 시작
        try {
            spaRecommendationDto.setImageUrl(imageUrlForHistory); // 프론트로 보낼 URL
            String jsonStr = objectMapper.writeValueAsString(spaRecommendationDto); // DB 저장을 위해 json 문자열 사용

            AiRecommendHistory saved = aiRecommendHistoryService.saveSuccess(
                    userEmail, imageUrlForHistory, detectedBreed,
                    true, jsonStr, question
            );

            if (saved != null) {
                log.info("AI 추천 기록 저장 완료 → id={}, user={}, breed={}", saved.getId(), userEmail, detectedBreed);
                // 저장된 메타를 DTO에 반영
                spaRecommendationDto.setId(saved.getId());
                spaRecommendationDto.setCreatedAt(saved.getCreatedAt());
            }

        } catch (Exception e) {
            log.warn("AI 추천 기록 저장 중 알 수 없는 오류: {}", e.getMessage());
        }

        return spaRecommendationDto; // 그대로 반환
    }

}