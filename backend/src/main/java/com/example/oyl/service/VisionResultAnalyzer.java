package com.example.oyl.service;

import com.example.oyl.dto.VisionAnalysisResult;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class VisionResultAnalyzer {

    private static final String UNKNOWN_BREED = "알 수 없는 견종";

    // 최소 60% 이상 일 때만 강아지로 인정
    private static final float DOG_OBJECT_MIN_SCORE = 0.6f;

    // 금지어 목록
    private static final List<String> BANNED_LABELS = List.of(
            "clothes", "costume", "pet supply", "clothing", "supply"
    );

    // 라벨 도메인 신호(개/견 관련, 속성 관련)
    private static final List<String> POSITIVE_LABEL_HINTS = List.of(
            "dog","puppy","canine","강아지","반려견","견",
            "coat","털","毛","shortcoat","longcoat","doublecoat",
            "small","medium","large","소형","중형","대형"
    );

    // 비전 라벨들을 분석해서 유효한 데이터인지 검증하고 바구니에 담아 리턴
    public VisionAnalyzeResult analyze(VisionAnalysisResult visionResult) {
        // 0) null 체크 가드
        if (visionResult == null) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "Vision API 응답이 유효하지 않습니다.");
        }

        // 0-1) 객체탐지 결과 수신 여부 진단 로그
        if (visionResult.getObjects() == null) {
            log.error("[Vision] localizedObjectAnnotations가 null입니다. REQUEST 옵션을 확인해주세요.");

            throw new CustomException(
                    ErrorCode.AI_ANALYSIS_FAILED,
                    "이미지 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
        }

        // 1) 강아지 여부 1차 필터
        if (!visionResult.isDog()) {
            throw new CustomException(ErrorCode.NOT_A_DOG_IMAGE);
        }

        // 2) 마릿수 판단 (0.6 이상 점수만)
        long dogBoxCount = visionResult.getObjects().stream()
                .filter(o -> o.getName() != null && o.getName().equalsIgnoreCase("Dog"))
                .filter(o -> o.getScore() == null || o.getScore() >= DOG_OBJECT_MIN_SCORE)
                .count();

        log.info("객체탐지 Dog 박스 수: {}", dogBoxCount);

        // 2-1) 라벨 중 금지어 제거 및 가공
        // 구글 클라이언트가 이미 String 리스트로 줬기 때문에 getDescription()을 호출할 필요없음!
        List<String> visionLabels = Optional.ofNullable(visionResult.getLabels()).orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .filter(desc -> BANNED_LABELS.stream().noneMatch(bad -> desc.toLowerCase().contains(bad)))
                .toList();

        // 복수/그룹 시그널 검사
        boolean pluralSignal = visionLabels.stream()
                .map(String::toLowerCase)
                .anyMatch(d -> d.contains("dogs") || d.contains("group"));

        if (dogBoxCount > 1 || (dogBoxCount == 0 && pluralSignal)) {
            throw new CustomException(ErrorCode.MULTIPLE_DOG_DETECTED);
        }

        // 3) 견종 설정 조율 (감독관을 먼저 통과시키기!)
        String rawBreed = visionResult.getDetectedBreed();
        String detectedBreed;

        // 구글이 준 견종이 아예 없거나, '알 수 없는 패턴'의 지저분한 글자라면?
        if (rawBreed == null || isUnknownBreed(rawBreed)) {
            detectedBreed = UNKNOWN_BREED; // 깔끔하게 "알 수 없는 견종"으로 통일!
        } else {
            detectedBreed = rawBreed;      // 진짜 올바른 견종일 때만 저장!
        }

        // 4) 보조 라벨 사용 가능 여부 판정
        // 이제 detectedBreed는 무조건 "알 수 없는 견종"이거나 "진짜 견종" 둘 중 하나만 가짐!
        boolean labelsUsable = visionLabels.stream()
                .map(String::toLowerCase)
                .anyMatch(label -> POSITIVE_LABEL_HINTS.stream().anyMatch(label::contains));

        boolean visionBreedUsable = !detectedBreed.equals(UNKNOWN_BREED);

        log.info("[Analyzer] 분석 완료 -> 최종 견종='{}', breedUsable={}, labelsUsable={}",
                detectedBreed, visionBreedUsable, labelsUsable);

        return new VisionAnalyzeResult(detectedBreed, visionLabels, visionBreedUsable, labelsUsable);
    }

    // 💡 단순 문자열 공백 정리 유틸
    private String norm(String s) {
        return s == null ? "" : s.trim();
    }

    // 💡 Vision unknown 판정 메서드 ("이 견종을 믿고 써도 되는지?")
    public boolean isUnknownBreed(String s) {
        String t = norm(s).toLowerCase(Locale.ROOT);       // 공백 제거, 소문자 변경
        if (t.isEmpty()) return true;                      // 글자가 없음 -> 견종 정보가 없으니 알 수 없음(=true, 끝)

        if (t.equals(UNKNOWN_BREED.toLowerCase(Locale.ROOT))) return true;  // "알 수 없는 견종" = true
        String compact = t.replaceAll("\\s+", "");          // 모든 공백(띄워쓰기) 제거
        if (compact.contains("알수없는")) return true;

        return t.contains("unknown") || t.contains("unidentified");          // unknown, unidentified가 포함?
    }

    // 메인 서비스가 꺼내 쓸 최종 결과 바구니 패키지
    public record VisionAnalyzeResult(
            String detectedBreed,
            List<String> visionLabels,
            boolean visionBreedUsable,
            boolean labelsUsable
    ) {}

}
