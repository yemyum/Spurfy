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

@Slf4j
@Service
@RequiredArgsConstructor
public class DogImageService {

    private final GoogleVisionClient googleVisionClient;
    private final GptClient gptClient;
    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;
    private final SpaServiceRepository spaServiceRepository;
    private final ObjectMapper objectMapper;

    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final String UNKNOWN_BREED = "알 수 없는 견종의 강아지";
    private static final String DEFAULT_AGE_GROUP = "성견";
    private static final String DEFAULT_ACTIVITY_LEVEL = "보통";
    private static final String CHECKLIST_NOT_SELECTED_BREED = "선택 안 함";
    private static final List<String> BANNED_LABELS = List.of("clothes", "costume", "pet supply", "clothing", "supply");
    private static final String IMAGE_FILE_NAME_FORMAT = "yyyyMMdd_HHmmssSSS";

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
        VisionAnalysisResult visionResult;
        String detectedBreed = UNKNOWN_BREED; // 기본값 설정
        List<String> visionLabels = new ArrayList<>(); // Vision API 라벨 (초기화)

        try {
            visionResult = googleVisionClient.analyzeImage(dogImageFile);

            if (!visionResult.isDog()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "사진 속 강아지를 찾아볼 수 없어요. 강아지 사진을 다시 올려주세요!");
            }

            if (visionResult.getDetectedBreed() != null && !visionResult.getDetectedBreed().isEmpty()) {
                detectedBreed = visionResult.getDetectedBreed();
            }

            // 1. 필터링 키워드(무시할 라벨) 미리 선언
            visionLabels = visionResult.getLabels().stream()
                    .map(label -> label.getDescription())
                    .filter(desc -> BANNED_LABELS.stream().noneMatch(bad -> desc.toLowerCase().contains(bad)))
                    .toList();

        } catch (CustomException e) {
            log.warn("Vision 분석 실패 → {}", e.getMessage());
            // CustomException 발생 시 detectedBreed는 기본값 유지, visionLabels는 비워둠
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "견종 분석 실패: " + e.getMessage());
        }

        log.info("Vision 분석 완료 → {}", detectedBreed);


        // ✅ 사용자 체크리스트를 파싱하고 GPT에 전달할 최종 값 결정하는 로직 시작
        // Checklist에서 파싱할 정보들
        String userSelectedBreed = null;
        String userSelectedAgeGroup = DEFAULT_AGE_GROUP; // "성견"을 상수로 대체
        String userSelectedActivityLevel = DEFAULT_ACTIVITY_LEVEL; // "보통"을 상수로 대체
        List<String> userSelectedHealthIssues = new ArrayList<>(); // 사용자 선택 건강 문제

        log.info("Received raw checklist string from frontend: {}", checklist);

        if (checklist != null && !checklist.trim().isEmpty()) {
            try {
                // Checklist 문자열을 Map으로 파싱 (Map<String, Object>로 받아서 List<String> 파싱 가능)
                Map<String, Object> parsedChecklist = objectMapper.readValue(checklist, Map.class);
                log.info("Parsed checklist map: {}", parsedChecklist);

                userSelectedBreed = (String) parsedChecklist.get("selectedBreed");
                userSelectedAgeGroup = (String) parsedChecklist.get("ageGroup");
                userSelectedActivityLevel = (String) parsedChecklist.get("activityLevel");

                // healthIssues는 List<String>으로 파싱될 수 있도록 처리
                Object healthIssuesObj = parsedChecklist.get("healthIssues");
                if (healthIssuesObj instanceof List) {
                    userSelectedHealthIssues = (List<String>) healthIssuesObj;
                }

            } catch (IOException e) {
                log.error("체크리스트 JSON 파싱 실패: {}", checklist, e);
            }
        }

        // 최종적으로 GPT에 전달할 견종, 연령대, 활동 수준 결정
        String finalBreedToUse = detectedBreed;
        // "선택 안 함"을 상수로 대체
        if (userSelectedBreed != null && !userSelectedBreed.isEmpty() && !CHECKLIST_NOT_SELECTED_BREED.equals(userSelectedBreed)) {
            finalBreedToUse = userSelectedBreed;
            log.info("사용자 선택 견종 ({})이 Vision API 결과 ({})보다 우선 적용됩니다.", userSelectedBreed, detectedBreed);
        }

        String finalAgeGroupToUse = userSelectedAgeGroup;
        String finalActivityLevelToUse = userSelectedActivityLevel;

        log.info("최종적으로 GPT에 전달될 견종: '{}', 연령대: '{}', 활동 수준: '{}'", finalBreedToUse, finalAgeGroupToUse, finalActivityLevelToUse);

        // Vision API 라벨과 사용자 선택 건강 문제를 결합 (중복 제거)
        HashSet<String> combinedHealthIssuesSet = new HashSet<>(userSelectedHealthIssues);
        for (String label : visionLabels) {
            String lowerCaseLabel = label.toLowerCase();
            if (lowerCaseLabel.contains("skin") || lowerCaseLabel.contains("dermatitis") || lowerCaseLabel.contains("itchy")) {
                combinedHealthIssuesSet.add("피부 문제 (Vision)");
            }
            if (lowerCaseLabel.contains("joint") || lowerCaseLabel.contains("arthritis") || lowerCaseLabel.contains("limp")) {
                combinedHealthIssuesSet.add("관절 문제 (Vision)");
            }
            if (lowerCaseLabel.contains("sick") || lowerCaseLabel.contains("ill") || lowerCaseLabel.contains("bandage")) {
                combinedHealthIssuesSet.add("건강 이상 (Vision)");
            }
            // TODO: 필요에 따라 더 많은 Vision 라벨-건강 문제 매핑 추가
        }
        List<String> finalHealthIssuesToUse = new ArrayList<>(combinedHealthIssuesSet);
        log.info("최종적으로 GPT에 전달될 건강 문제: {}", finalHealthIssuesToUse);

        String finalAdjActivity = toAdjective(finalActivityLevelToUse); // "활발함" → "활발한"
        String breedForPrompt = UNKNOWN_BREED.equals(finalBreedToUse) ? "모름" : finalBreedToUse;


        // ✅ 결정된 값들을 바탕으로 GPT API를 호출하고 응답을 처리하는 로직 시작
        GptSpaRecommendationResponseDTO spaRecommendationDto;
        try {
            log.info("GPT 호출 결정 - finalBreedToUse: '{}'", finalBreedToUse);

            // 공통 필드 변수들을 미리 준비
            String ageGroup = finalAgeGroupToUse;
            List<String> healthIssues = finalHealthIssuesToUse;
            String activityLevel = finalAdjActivity;

            // Vision API 결과에 따라 다른 GPT 클라이언트를 호출
            if (UNKNOWN_BREED.equals(detectedBreed)) { // Vision이 못 맞추고 사용자도 선택 안 했을 때
                log.info("Calling gptClient.recommendSpaByLabels...");
                // 라벨 기반 추천 DTO에 공통 필드와 특정 필드를 모두 넣어 빌드
                SpaLabelRecommendationRequestDTO labelDto = SpaLabelRecommendationRequestDTO.builder()
                        .labels(visionLabels)
                        .ageGroup(ageGroup)
                        .skinTypes(List.of())
                        .healthIssues(healthIssues)
                        .activityLevel(activityLevel)
                        .checklist(checklist)
                        .question(question)
                        .breed(breedForPrompt)
                        .build();

                spaRecommendationDto = gptClient.recommendSpaByLabels(labelDto); // DTO로 받음
            } else {  // Vision이 맞추거나 사용자가 선택했을 때
                log.info("Calling gptClient.recommendSpa...");
                // 견종 기반 추천 DTO에 공통 필드와 특정 필드를 모두 넣어 빌드
                SpaRecommendationRequestDTO request = SpaRecommendationRequestDTO.builder()
                        .breed(breedForPrompt) // 사용자 선택 견종 또는 Vision 인식 견종이 전달됨
                        .ageGroup(ageGroup)
                        .skinTypes(List.of())
                        .healthIssues(healthIssues)
                        .activityLevel(activityLevel)
                        .checklist(checklist)
                        .question(question)
                        .build();

                spaRecommendationDto = gptClient.recommendSpa(request); // DTO로 받음
            }

            // ⭐⭐ GPT 응답 후 spaSlug가 null일 경우 DB에서 찾아 채워넣는 로직 추가 ⭐⭐
            if (spaRecommendationDto != null && spaRecommendationDto.getSpaSlug() == null && spaRecommendationDto.getSpaName() != null) {
                String cleanSpaName = spaRecommendationDto.getSpaName()
                        .replace("**", "")
                        .replace("🧘‍♀️ ", "")
                        .replace("🌸 ", "")
                        .replace("🛁 ", "")
                        .replace("🌿 ", "")
                        .replace("에요!", "")
                        .trim();

                // DB에서 스파 이름으로 SpaService 엔티티를 찾음
                Optional<SpaService> foundSpa = spaServiceRepository.findByName(cleanSpaName);

                // 찾았다면 해당 스파의 slug를 DTO에 설정
                foundSpa.ifPresent(spaService -> {
                    spaRecommendationDto.setSpaSlug(spaService.getSlug());
                    log.info("DB에서 spaSlug 찾아서 채워넣음: {}", spaService.getSlug());
                });
            }

            // 최종 출력 문구 중복 수식어 방지 (LLM이 실수해도 한 번 더 정리)
            if (spaRecommendationDto != null) {
                spaRecommendationDto.setIntro(dedupeKo(spaRecommendationDto.getIntro()));
                spaRecommendationDto.setCompliment(dedupeKo(spaRecommendationDto.getCompliment()));
                spaRecommendationDto.setRecommendationHeader(dedupeKo(spaRecommendationDto.getRecommendationHeader()));
                spaRecommendationDto.setSpaName(dedupeKo(spaRecommendationDto.getSpaName()));
                if (spaRecommendationDto.getSpaDescription() != null) {
                    List<String> cleaned = spaRecommendationDto.getSpaDescription().stream()
                            .map(this::dedupeKo)
                            .toList();
                    spaRecommendationDto.setSpaDescription(cleaned);
                }
                spaRecommendationDto.setClosing(dedupeKo(spaRecommendationDto.getClosing()));
            }

        } catch (Exception e) {
            log.error("GPT 호출 실패", e);
            throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED, "스파 추천 실패: " + e.getMessage());
        }

        log.info("GPT 추천 DTO 완료 → {}", spaRecommendationDto);


        // ✅ AI 추천 기록을 DB에 저장하는 로직 시작
        try {
            String imageUrlForHistory = "/api/images/" + savedFileName;

            spaRecommendationDto.setImageUrl(imageUrlForHistory); // 프론트로 보낼 URL

            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrlForHistory)
                    .detectedBreed(detectedBreed)
                    .isDog(true)
                    .recommendResult(objectMapper.writeValueAsString(spaRecommendationDto))
                    .prompt(question)
                    .errorMessage(null)
                    .build();

            AiRecommendHistory savedHistory = aiRecommendHistoryRepository.save(history); // DB에 저장하고, 저장된 객체를 받아오기!
            log.info("AI 추천 기록 DB 저장 완료 → user={}, breed={}", userEmail, detectedBreed);

            // ⭐⭐⭐ 저장된 history 객체에서 id와 createdAt을 꺼내서 spaRecommendationDto에 넣어줌! ⭐⭐⭐
            spaRecommendationDto.setId(savedHistory.getId());
            spaRecommendationDto.setCreatedAt(savedHistory.getCreatedAt());
        } catch (Exception e) {
            log.warn("AI 추천 기록 저장 실패 → {}", e.getMessage());
        }

        return spaRecommendationDto; // DTO 객체를 그대로 반환
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

}