package com.example.oyl.service;

import com.example.oyl.client.GoogleVisionClient;
import com.example.oyl.client.GptClient;
import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.domain.SpaService;
import com.example.oyl.dto.GptSpaRecommendationResponseDTO;
import com.example.oyl.dto.SpaLabelRecommendationRequestDTO;
import com.example.oyl.dto.SpaRecommendationRequestDTO;
import com.example.oyl.dto.VisionAnalysisResult;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import com.example.oyl.repository.SpaServiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DogImageService {

    private final GoogleVisionClient googleVisionClient;
    private final GptClient gptClient;
    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;
    private final SpaServiceRepository spaServiceRepository;
    private final ObjectMapper objectMapper;

    // 파일 업로드
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final String IMAGE_FILE_NAME_FORMAT = "yyyyMMdd_HHmmssSSS";

    // ===== constants =====
    private static final String UNKNOWN_BREED = "알 수 없는 견종";
    private static final String DEFAULT_AGE_GROUP = "성견";
    private static final String DEFAULT_ACTIVITY_LEVEL = "보통";

    private static final List<String> BANNED_LABELS = List.of(
            "clothes", "costume", "pet supply", "clothing", "supply"
    );
    private static final float DOG_OBJECT_MIN_SCORE = 0.6f;
    private static final String IMAGE_BASE_PATH = "/api/images/";

    // ===== tiny utils (only what we actually use) =====
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

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<String> safeGetList(Object v) {
        if (v instanceof java.util.List<?> l) {
            return l.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .toList(); // 자바 17 OK
        }
        return java.util.List.of();
    }

    // AI 호출 횟수
    private static final int MAX_DAILY_AI_CALLS = 10;  // 실제 서비스는 5번 이하로 수정해두기

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


        // ✅ 이미지 파일을 서버에 저장하는 로직 시작
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();
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
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(IMAGE_FILE_NAME_FORMAT)) + fileExtension;

            Path filePath = uploadPath.resolve(savedFileName);
            Files.copy(dogImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("이미지 저장 완료 → {}", filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "파일 저장 중 오류 발생: " + e.getMessage());
        }


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

            // 1) 강아지 여부 1차 필터 (라벨 보조) -> !isDog()일 때 예외
            if (!visionResult.isDog()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "사진 속 강아지를 찾아볼 수 없어요. 강아지 사진을 다시 올려주세요!");
            }

            // 2) 객체탐지 결과로 마릿수 판단
            long dogBoxCount = Optional.ofNullable(visionResult.getObjects())
                    .orElse(List.of())
                    .stream()
                    .filter(o -> o.getName() != null && o.getName().equalsIgnoreCase("Dog"))
                    .filter(o -> o.getScore() == null || o.getScore() >= DOG_OBJECT_MIN_SCORE) // ← 0.6f 상수 쓰기
                    .count();

            log.info("객체탐지 Dog 박스 수: {}", dogBoxCount);

            // 2-1) 보조 규칙: ban 라벨 제거(+원본 케이스 유지)
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
                        "여러 마리 강아지가 감지되었어요. 반려견의 정면이 담긴 단독 사진이여야 AI가 정확하게 인식해요!"
                );
            }

            // 3) 견종 설정
            if (visionResult.getDetectedBreed() != null && !visionResult.getDetectedBreed().isEmpty()) {
                detectedBreed = visionResult.getDetectedBreed();
            }

        } catch (CustomException e) {
            log.warn("Vision 분석 실패 → {}", e.getMessage());

            // 실패 기록 저장
            final String imageUrlForHistory = IMAGE_BASE_PATH + savedFileName;
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
        // Checklist에서 파싱할 정보들
        String userSelectedBreed = null;
        String userSelectedAgeGroup = DEFAULT_AGE_GROUP; // "성견"을 상수로 대체
        String userSelectedActivityLevel = DEFAULT_ACTIVITY_LEVEL; // "보통"을 상수로 대체
        List<String> userSelectedHealthIssues = new ArrayList<>(); // 사용자 선택 건강 문제

        log.info("[Checklist] raw(len={}): {}", checklist == null ? 0 : checklist.length(), truncate(checklist, 500));

        if (checklist != null && !checklist.trim().isEmpty()) {
            try {
                Map<String, Object> parsedChecklist = objectMapper.readValue(checklist, Map.class);
                log.info("[Checklist] parsed: {}", parsedChecklist);

                String v;

                v = (String) parsedChecklist.get("selectedBreed");
                if (v != null && !v.isBlank() && !"선택 안 함".equals(v)) {
                    userSelectedBreed = v.trim();
                }

                v = (String) parsedChecklist.get("ageGroup");
                if (v != null && !v.isBlank()) {
                    userSelectedAgeGroup = v.trim();
                }

                v = (String) parsedChecklist.get("activityLevel");
                if (v != null && !v.isBlank()) {
                    userSelectedActivityLevel = v.trim();
                }

                Object hiObj = parsedChecklist.get("healthIssues");
                if (hiObj instanceof List<?> l && !l.isEmpty()) {
                    userSelectedHealthIssues = l.stream()
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList(); // 자바 17 OK
                }

            } catch (IOException e) {
                log.error("체크리스트 JSON 파싱 실패: {}", checklist, e);
            }
        }
        log.info("[Checklist] decided → breed='{}', age='{}', act='{}', issues={}",
                userSelectedBreed, userSelectedAgeGroup, userSelectedActivityLevel,
                userSelectedHealthIssues == null ? 0 : userSelectedHealthIssues.size());


        // ✅ Vision 자체가 유효한가? (먼저 계산)
        boolean visionBreedUsable = !isUnknownBreed(detectedBreed);

        // ✅ 견종: Vision 결과가 유효하면 우선, 아니면 사용자 선택
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

        // 3) 건강 이슈: 사용자 선택 입력만 사용
        List<String> finalHealthIssuesToUse =
                new ArrayList<>(Optional.ofNullable(userSelectedHealthIssues).orElse(List.of()));

        // 4) 보조 필드
        String finalAdjActivity = toAdjective(finalActivityLevelToUse); // "활발함" → "활발한"
        String breedForPrompt = finalBreedToUse; // 재선언 금지: 위에서 hasUsableBreed 이미 계산됨

        // 5) 로그 최소화
        log.info("[GPT-IN] breed='{}'(usable={}), age='{}', act='{}', issues={}",
                breedForPrompt, hasUsableBreed, finalAgeGroupToUse, finalAdjActivity, finalHealthIssuesToUse);

        boolean labelsUsable = visionLabels != null && !visionLabels.isEmpty();
        if (!hasUsableBreed && !labelsUsable) {
            log.info("GPT 차단: breed/labels 부적합 → 안내만 반환");
            final String imageUrlForHistory = IMAGE_BASE_PATH + savedFileName;
            return GptSpaRecommendationResponseDTO.createFailureResponse(
                    "사진 정보가 부족합니다. 반려견 정면이 담긴 단독 사진으로 다시 올려주세요!", imageUrlForHistory
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
            if (!visionBreedUsable) { // Vision unknown → 라벨 기반(추천스파 by labels)
                log.info("Calling gptClient.recommendSpaByLabels...");

                // (보너스 안전망) 라벨 비었으면 여기서도 한번 더 컷
                if (!labelsUsable) {

                    // 실패 기록 저장
                    final String imageUrlForHistory = IMAGE_BASE_PATH + savedFileName;
                    AiRecommendHistory history = AiRecommendHistory.builder()
                            .userId(userEmail)
                            .imageUrl(imageUrlForHistory)
                            .detectedBreed(detectedBreed)
                            .isDog(true) // 여기선 강아지 인식은 됐으니 true
                            .recommendResult(null)
                            .prompt(question)
                            .errorMessage("사진 정보가 부족합니다. 반려견 정면이 담긴 단독 사진으로 다시 올려주세요!")
                            .build();
                    aiRecommendHistoryRepository.save(history);

                    return GptSpaRecommendationResponseDTO.createFailureResponse(
                            "사진 정보가 부족합니다. 반려견 정면이 담긴 단독 사진으로 다시 올려주세요!", imageUrlForHistory
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

            } else {  // 견종 확정됨 → 견종 기반
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

            // 3) 널 가드
            if (spaRecommendationDto == null) {
                throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED,
                        "AI 추천 결과를 가져오지 못했어요. 잠시 후 다시 시도해주세요.");
            }

            // 4) spaSlug 보정 (DB lookup)
            Optional.ofNullable(spaRecommendationDto).ifPresent(dto -> {
                if (dto.getSpaSlug() == null && dto.getSpaName() != null) {
                    String cleanSpaName = normalizeSpaName(dto.getSpaName());
                    spaServiceRepository.findByName(cleanSpaName).ifPresent(spa ->
                            dto.setSpaSlug(spa.getSlug())
                    );
                }
            });

            // 5) 출력 문구 후처리 (중복 수식어 정리)
            spaRecommendationDto.setIntro(dedupeKo(spaRecommendationDto.getIntro()));
            spaRecommendationDto.setCompliment(dedupeKo(spaRecommendationDto.getCompliment()));
            spaRecommendationDto.setRecommendationHeader(dedupeKo(spaRecommendationDto.getRecommendationHeader()));
            spaRecommendationDto.setSpaName(dedupeKo(spaRecommendationDto.getSpaName()));
            if (spaRecommendationDto.getSpaDescription() != null) {
                spaRecommendationDto.setSpaDescription(
                        spaRecommendationDto.getSpaDescription().stream()
                                .map(this::dedupeKo)
                                .toList()
                );
            }
            spaRecommendationDto.setClosing(dedupeKo(spaRecommendationDto.getClosing()));

        } catch (Exception e) {
            log.error("예상치 못한 GPT 호출 실패", e);
            throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED,
                    "AI 서비스가 현재 불안정합니다. 잠시 후 다시 시도해 주세요.");
        }

        // 예외 없이 try문이 끝났을 때만 실행
        log.info("GPT 추천 DTO 완료 → {}", spaRecommendationDto);


        // ✅ AI 추천 기록을 DB에 저장하는 로직 시작
        try {
            final String imageUrlForHistory = IMAGE_BASE_PATH + savedFileName;
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

    // 활동성 어미 정규화: 활발함→활발한, 차분함→차분한
    private String toAdjective(String s) {
        if (s == null) return "";
        return s.replaceAll("함$", "한");
    }

    // 한국어 중복 수식어 제거: "활발하고 활발한" → "활발한"
    private String dedupeKo(String text) {
        if (text == null) return null;
        text = text.replaceAll("([가-힣]+)\\s*하고\\s*\\1(한|인|함)", "$1$2");
        text = text.replaceAll("([가-힣]+)\\s*하고\\s*\\1\\b", "$1");
        text = text.replaceAll("(\\b[가-힣]+)\\s+\\1(한|인|함)?", "$1$2");
        return text;
    }

    // spaName 정리: 마크다운/이모지/따옴표/끝맺음 표현/여러 공백 제거
    private String normalizeSpaName(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("(\\*\\*|[🧘‍♀️🌸🛁🌿]|\"|에?요!?)", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

}