package com.example.oyl.service;

import com.example.oyl.dto.GptSpaRecommendationResponseDTO;
import com.example.oyl.util.ChecklistParser;
import com.example.oyl.util.ChecklistResult;
import com.example.oyl.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RecommendationProcessor {

    private static final String UNKNOWN_BREED = "알 수 없는 견종";

    // 파싱 + 조건 정제 로직 (GPT용)
    public RefinedGptInputData refineInputData(String checklistJson, String detectedBreed) {

        // 1. 파서로 안전하게 JSON 파싱
        ChecklistResult checklistResult = ChecklistParser.parse(checklistJson);

        // 2. 견종 우선권 조율
        boolean visionBreedUsable = !isUnknownBreed(detectedBreed);

        // Vision > 사용자 선택 > 모름 순서로 우선권
        String finalBreedToUse;
        if (visionBreedUsable) {
            finalBreedToUse = norm(detectedBreed);
        } else if (checklistResult.breed != null && !checklistResult.breed.isBlank()) {
            finalBreedToUse = checklistResult.breed.trim();
        } else {
            finalBreedToUse = UNKNOWN_BREED;
        }

        // 3. 연령 / 활동성 / 건강이슈 정제
        String finalAgeGroupToUse = norm(checklistResult.ageGroup);
        String finalAdjActivity = TextUtils.toAdjective(norm(checklistResult.activityLevel));  // 형용사 형태 변환
        List<String> finalHealthIssuesToUse = new ArrayList<>(
                Optional.ofNullable(checklistResult.healthIssues).orElse(List.of())  // null 방어
        );

        log.info("[Processor] 데이터 정제 완료 -> breed='{}', age='{}', act='{}', issues={}",
                finalBreedToUse, finalAgeGroupToUse, finalAdjActivity, finalHealthIssuesToUse.size());

        return new RefinedGptInputData(finalBreedToUse, finalAgeGroupToUse, finalAdjActivity, finalHealthIssuesToUse, checklistResult.breed);
    }

    private String norm(String s) { return s == null ? "" : s.trim(); }

    private boolean isUnknownBreed(String s) {
        String t = norm(s).toLowerCase(java.util.Locale.ROOT);
        if (t.isEmpty()) return true;
        if (t.equals(UNKNOWN_BREED.toLowerCase(java.util.Locale.ROOT))) return true;
        if (t.replaceAll("\\s+", "").contains("알수없는")) return true;
        return t.contains("unknown") || t.contains("unidentified");
    }

    // 메인 서비스가 편하게 쓸 수 있도록 묶은 바구니(record)
    public record RefinedGptInputData(
            String breedForPrompt,
            String finalAgeGroup,
            String finalAdjActivity,
            List<String> finalHealthIssues,
            String userSelectedBreed // 라벨 GPT 빌더용 보조 데이터
    ) {}

    // 지저분한 텍스트 중복 제거(후처리) 세팅만 따로 모은 메서드
    public void cleanUpResponseText(GptSpaRecommendationResponseDTO dto) {
        if (dto == null) return;   // 데이터가 없다면 패스

        dto.setIntro(TextUtils.dedupeKo(dto.getIntro()));
        dto.setCompliment(TextUtils.dedupeKo(dto.getCompliment()));
        dto.setRecommendationHeader(TextUtils.dedupeKo(dto.getRecommendationHeader()));
        dto.setSpaName(TextUtils.dedupeKo(dto.getSpaName()));

        if (dto.getSpaDescription() != null) {
            dto.setSpaDescription(
                    dto.getSpaDescription().stream()
                            .map(TextUtils::dedupeKo)
                            .toList()
            );
        }

        dto.setClosing(TextUtils.dedupeKo(dto.getClosing()));
    }

}
