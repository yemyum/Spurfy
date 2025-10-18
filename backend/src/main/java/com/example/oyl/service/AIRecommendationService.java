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
    private final SpaServiceRepository spaServiceRepository;
    private final ObjectMapper objectMapper;
    private final ImageStorageUtil imageStorageUtil;

    // 견종 관련
    private static final String UNKNOWN_BREED = "알 수 없는 견종";

    private static final List<String> BANNED_LABELS = List.of(
            "clothes", "costume", "pet supply", "clothing", "supply"
    );
    private static final float DOG_OBJECT_MIN_SCORE = 0.6f;

    // 라벨 도메인 신호(개/견 관련, 속성 관련)
    private static final List<String> POSITIVE_LABEL_HINTS = List.of(
            "dog","puppy","canine","강아지","반려견","견",
            "coat","털","毛","shortcoat","longcoat","doublecoat",
            "small","medium","large","소형","중형","대형"
    );

    private static String norm(String s) { return s == null ? "" : s.trim(); }

    // Vision unknown 판정: 상수/한글/영문 변형 커버
    private static boolean isUnknownBreed(String s) {
        String t = norm(s).toLowerCase(java.util.Locale.ROOT);
        if (t.isEmpty()) return true;

        if (t.equals(UNKNOWN_BREED.toLowerCase(java.util.Locale.ROOT))) return true;
        String compact = t.replaceAll("\\s+", ""); // "알수없는견종" 케이스
        if (compact.contains("알수없는")) return true;

        return t.contains("unknown") || t.contains("unidentified");
    }

    public int getTodayAiCallCount(String userEmail) {
        // analyzeAndRecommendSpa와 동일한 로직으로 오늘 날짜 범위 설정
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);

        // Repository를 사용해 오늘 횟수 조회
        long todayCount = aiRecommendHistoryRepository.countByUserIdAndCreatedAtBetween(userEmail, startOfDay, endOfDay);

        // 서비스 로직을 그대로 재활용하는 것이 핵심!
        return (int) todayCount;
    }

    // AI 호출 횟수
    private static final int MAX_DAILY_AI_CALLS = 3;  // 실제 서비스는 5번 이하로 수정해두기

    // 대화 횟수 감지 + 이미지 저장 -> vision 사진 분석 -> GPT 추천
    public GptSpaRecommendationResponseDTO analyzeAndRecommendSpa(MultipartFile dogImageFile, String userEmail, String checklist, String question) {

        if (dogImageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);

        long todayCount = aiRecommendHistoryRepository.countByUserIdAndCreatedAtBetween(userEmail, startOfDay, endOfDay);

        if (todayCount >= MAX_DAILY_AI_CALLS) {
            log.warn("DogImageService.analyzeAndRecommendSpa - AI 대화 횟수 제한 초과! 현재 호출 횟수: {}, 최대 허용: {}", todayCount, MAX_DAILY_AI_CALLS);
            throw new CustomException(ErrorCode.CONVERSATION_LIMIT_EXCEEDED,
                    "하루 AI 대화 횟수(" + MAX_DAILY_AI_CALLS + "회)를 초과했습니다. 내일 다시 시도해주세요.");
        }


        // ✅ 파일 저장
        String savedFileName = imageStorageUtil.save(dogImageFile);
        final String imageUrlForHistory = "/api/images/" + savedFileName;


        // ✅ Google Vision API를 호출하여 이미지 분석하는 로직 시작
        VisionAnalysisResult visionResult = null;
        String detectedBreed = UNKNOWN_BREED; // 기본값 설정
        List<String> visionLabels = List.of(); // 선언과 동시에 초기화(컴파일러 경고 방지)

        try {
            visionResult = googleVisionClient.analyzeImage(dogImageFile);

            // 0) visionResult가 null일 경우를 먼저 막아야 함
            if (visionResult == null) {
                throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "Vision API 응답이 유효하지 않습니다.");
            }

            // 0-1) 객체탐지 결과 수신 여부 진단 로그
            if (visionResult.getObjects() == null) {
                log.warn("[Vision] localizedObjectAnnotations가 null입니다. " +
                        "REQUEST에 OBJECT_LOCALIZATION 피처가 빠졌을 수 있어요.");
            }

            // 1) 강아지 여부 1차 필터 : "강아지가 맞는가?" (라벨 보조) -> !isDog()일 때 예외
            if (!visionResult.isDog()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "사진 속에서 반려견을 찾지 못했어요. AI가 헷갈리지 않도록 반려견의 정면이 잘 보이는 사진으로 다시 부탁드려요!");
            }

            // 2) 객체탐지 결과로 마릿수 판단, 0.6 이상 점수인 것만 카운트
            long dogBoxCount = Optional.ofNullable(visionResult.getObjects())
                    .orElse(List.of())
                    .stream()
                    .filter(o -> o.getName() != null && o.getName().equalsIgnoreCase("Dog"))
                    .filter(o -> o.getScore() == null || o.getScore() >= DOG_OBJECT_MIN_SCORE)
                    .count();

            log.info("객체탐지 Dog 박스 수: {}", dogBoxCount);

            // 2-1) 라벨 중 금지어 제거
            visionLabels = Optional.ofNullable(visionResult.getLabels()).orElse(List.of())
                    .stream()
                    .map(l -> l.getDescription())
                    .filter(Objects::nonNull)
                    .filter(desc -> BANNED_LABELS.stream().noneMatch(bad -> desc.toLowerCase().contains(bad)))
                    .toList();

            // 복수/그룹 시그널만 소문자로 검사
            boolean pluralSignal = visionLabels.stream()
                    .map(s -> s.toLowerCase())
                    .anyMatch(d -> d.contains("dogs") || d.contains("group"));

            if (dogBoxCount > 1 || (dogBoxCount == 0 && pluralSignal)) {
                throw new CustomException(
                        ErrorCode.INVALID_INPUT,
                        "여러 마리의 반려견이 감지되었어요. 한 마리의 반려견 정면이 담긴 단독 사진이여야 정확한 추천이 가능해요!"
                );
            }

            // 3) 견종 설정
            if (visionResult.getDetectedBreed() != null && !visionResult.getDetectedBreed().isEmpty()) {
                detectedBreed = visionResult.getDetectedBreed();
            }

        } catch (CustomException e) {
            log.warn("Vision 분석 실패 → {}", e.getMessage());

            // 실패 기록 저장
            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrlForHistory)
                    .detectedBreed(detectedBreed)
                    .isDog(false)
                    .recommendResult(null) // 추천 결과 없음
                    .prompt(question)
                    .errorMessage(e.getMessage()) // 실패 메시지 저장
                    .build();
            aiRecommendHistoryRepository.save(history);

            return GptSpaRecommendationResponseDTO.createFailureResponse(e.getMessage(), imageUrlForHistory);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "견종 분석 실패: " + e.getMessage());
        }

        log.info("Vision 분석 완료 → breed={}, labels={}", detectedBreed, visionLabels);


        // ✅ 사용자 체크리스트를 파싱하고 GPT에 전달할 최종 값 결정하는 로직 시작
        ChecklistResult checklistResult = ChecklistParser.parse(checklist);

        String userSelectedBreed = checklistResult.breed;
        String userSelectedAgeGroup = checklistResult.ageGroup;
        String userSelectedActivityLevel = checklistResult.activityLevel;
        List<String> userSelectedHealthIssues = checklistResult.healthIssues;

        log.info("[Checklist] decided → breed='{}', age='{}', act='{}', issues={}",
                userSelectedBreed, userSelectedAgeGroup, userSelectedActivityLevel,
                userSelectedHealthIssues == null ? 0 : userSelectedHealthIssues.size());


        // ✅ Vision이 뱉은 견종이 "모름(unknown)"이 아니면 가능
        boolean visionBreedUsable = !isUnknownBreed(detectedBreed);

        // ✅ Vision > 사용자 선택 > 모름 순서로 우선권
        String finalBreedToUse;
        if (visionBreedUsable) {
            finalBreedToUse = norm(detectedBreed);               // Vision 우선
        } else if (userSelectedBreed != null && !userSelectedBreed.isBlank()) {
            finalBreedToUse = userSelectedBreed.trim();          // Vision unknown → 사용자 선택
        } else {
            finalBreedToUse = UNKNOWN_BREED;
        }
        boolean hasUsableBreed = !isUnknownBreed(finalBreedToUse);

        // 2) 연령/활동성: 공백 정리만
        String finalAgeGroupToUse = norm(userSelectedAgeGroup);
        String finalActivityLevelToUse = norm(userSelectedActivityLevel);

        // 3) 건강 이슈: 사용자 선택 입력만 사용, 없으면 빈 리스트
        List<String> finalHealthIssuesToUse =
                new ArrayList<>(Optional.ofNullable(userSelectedHealthIssues).orElse(List.of()));

        // 4) 보조 필드
        // 활동성 형용사화
        String finalAdjActivity = TextUtils.toAdjective(finalActivityLevelToUse);
        String breedForPrompt = finalBreedToUse; // 재선언 금지: 위에서 hasUsableBreed 이미 계산됨

        // 5) GPT 호출 전에 지금 뭐 들고 있는지
        log.info("[GPT-IN] breed='{}'(usable={}), age='{}', act='{}', issues={}",
                breedForPrompt, hasUsableBreed, finalAgeGroupToUse, finalAdjActivity, finalHealthIssuesToUse);

        // 강아지 관련 단서만 있다면 GPT 추천 허용
        boolean labelsUsable = visionLabels != null
                && !visionLabels.isEmpty()
                && visionLabels.stream()
                .map(s -> norm(s))
                .anyMatch(d -> POSITIVE_LABEL_HINTS.stream().anyMatch(d::contains));

        // AI가 견종도 모르겠고, 다른 강아지 관련 단서(라벨)도 못 찾았을 때 : "강아지 사진은 맞는데, 내가 추천을 해줄 만큼 정보가 충분한지?"
        if (!hasUsableBreed && !labelsUsable) {
            log.info("GPT 차단: breed/labels 부적합 → 안내만 반환");
            return GptSpaRecommendationResponseDTO.createFailureResponse(
                    "사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!", imageUrlForHistory
            );
        }

        // ✅ 결정된 값들을 바탕으로 GPT API를 호출하고 응답을 처리하는 로직 시작
        GptSpaRecommendationResponseDTO spaRecommendationDto;
        try {

            // 1) 로그: 핵심만
            log.info("[GPT-IN] breed='{}'(usable={}), age='{}', act='{}', issuesCnt={}",
                    breedForPrompt, hasUsableBreed, finalAgeGroupToUse, finalAdjActivity,
                    finalHealthIssuesToUse != null ? finalHealthIssuesToUse.size() : 0);

            // Vision API 결과에 따라 다른 GPT 클라이언트를 호출
            if (!visionBreedUsable) { // Vision이 견종 모름 → 라벨 기반 GPT 호출
                log.info("Calling gptClient.recommendSpaByLabels...");

                // Vision도 모름 + 라벨 단서도 없음 -> 실패 처리
                if (!labelsUsable) {

                    // 실패 기록 저장
                    AiRecommendHistory history = AiRecommendHistory.builder()
                            .userId(userEmail)
                            .imageUrl(imageUrlForHistory)
                            .detectedBreed(detectedBreed)
                            .isDog(true) // 여기선 강아지 인식은 됐으니 true
                            .recommendResult(null)
                            .prompt(question)
                            .errorMessage("사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!")
                            .build();
                    aiRecommendHistoryRepository.save(history);

                    return GptSpaRecommendationResponseDTO.createFailureResponse(
                            "사진 정보가 부족합니다. 반려견의 정면이 담긴 단독 사진으로 다시 올려주세요!", imageUrlForHistory
                    );
                }

                SpaLabelRecommendationRequestDTO labelDto = SpaLabelRecommendationRequestDTO.builder()
                        .labels(Optional.ofNullable(visionLabels).orElse(List.of()))
                        .ageGroup(finalAgeGroupToUse)
                        .skinTypes(List.of())
                        .healthIssues(Optional.ofNullable(finalHealthIssuesToUse).orElse(List.of()))
                        .activityLevel(finalAdjActivity)
                        .checklist(checklist)
                        .question(question)
                        .breed(detectedBreed)              // Vision이 돌린 결과(여긴 unknown일 것)
                        .selectedBreed(userSelectedBreed)  // 보호자 선택
                        .build();

                spaRecommendationDto = gptClient.recommendSpaByLabels(labelDto);

            } else {  // Vision이 견종 확정 → 견종 기반 GPT 호출
                log.info("Calling gptClient.recommendSpa...");
                SpaRecommendationRequestDTO request = SpaRecommendationRequestDTO.builder()
                        .breed(detectedBreed)
                        .ageGroup(finalAgeGroupToUse)
                        .skinTypes(List.of())
                        .healthIssues(Optional.ofNullable(finalHealthIssuesToUse).orElse(List.of()))
                        .activityLevel(finalAdjActivity)
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

            // 4) spaSlug 보정 (DB lookup, GPT가 slug를 안줬다면 DB에서 찾아 채움)
            Optional.ofNullable(spaRecommendationDto).ifPresent(dto -> {
                if (dto.getSpaSlug() == null && dto.getSpaName() != null) {
                    String cleanSpaName = TextUtils.normalizeSpaName(dto.getSpaName());
                    spaServiceRepository.findByName(cleanSpaName).ifPresent(spa ->
                            dto.setSpaSlug(spa.getSlug())
                    );
                }
            });

            // 5) 출력 문구 후처리 (중복 수식어 정리)
            spaRecommendationDto.setIntro(TextUtils.dedupeKo(spaRecommendationDto.getIntro()));
            spaRecommendationDto.setCompliment(TextUtils.dedupeKo(spaRecommendationDto.getCompliment()));
            spaRecommendationDto.setRecommendationHeader(TextUtils.dedupeKo(spaRecommendationDto.getRecommendationHeader()));
            spaRecommendationDto.setSpaName(TextUtils.dedupeKo(spaRecommendationDto.getSpaName()));

            if (spaRecommendationDto.getSpaDescription() != null) {
                spaRecommendationDto.setSpaDescription(
                        spaRecommendationDto.getSpaDescription().stream()
                                .map(TextUtils::dedupeKo)
                                .toList()
                );
            }

            spaRecommendationDto.setClosing(TextUtils.dedupeKo(spaRecommendationDto.getClosing()));

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

            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrlForHistory)
                    .detectedBreed(detectedBreed)
                    .isDog(visionResult != null && visionResult.isDog())
                    .recommendResult(objectMapper.writeValueAsString(spaRecommendationDto))
                    .prompt(question)
                    .errorMessage(null)
                    .build();

            AiRecommendHistory saved = aiRecommendHistoryRepository.save(history);
            log.info("AI 추천 기록 저장 완료 → id={}, user={}, breed={}",
                    saved.getId(), userEmail, detectedBreed);

            // 저장된 메타를 DTO에 반영
            spaRecommendationDto.setId(saved.getId());
            spaRecommendationDto.setCreatedAt(saved.getCreatedAt());

        } catch (com.fasterxml.jackson.core.JsonProcessingException je) {
            log.warn("추천 결과 직렬화 실패: {}", je.getMessage());
        } catch (org.springframework.dao.DataAccessException de) {
            log.warn("AI 추천 기록 DB 저장 실패: {}", de.getMessage());
        } catch (Exception e) {
            log.warn("AI 추천 기록 저장 중 알 수 없는 오류: {}", e.getMessage());
        }

        return spaRecommendationDto; // 그대로 반환
    }

}