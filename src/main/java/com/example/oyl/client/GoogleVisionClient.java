package com.example.oyl.client;

import com.example.oyl.dto.GoogleVisionRequestDTO;
import com.example.oyl.dto.GoogleVisionResponseDTO;
import com.example.oyl.dto.VisionAnalysisResult;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleVisionClient {

    private final WebClient googleVisionWebClient;

    @Value("${google.vision.api-key}")
    private String visionApiKey;

    private static final Map<String, String> BREED_KOR_MAP = Map.ofEntries(
            Map.entry("Maltese", "말티즈"),
            Map.entry("Poodle", "푸들"),
            Map.entry("Golden Retriever", "골든리트리버"),
            Map.entry("Shih Tzu", "시츄"),
            Map.entry("Pomeranian", "포메라니안"),
            Map.entry("Labrador Retriever", "래브라도 리트리버")
            // ...필요시 추가
    );

    // 공통 유틸: 소문자 + 공백 제거
    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+","");
    }

    private static boolean isUnknownBreed(String t) {
        if (t == null || t.trim().isEmpty()) return true;
        String low = t.toLowerCase(java.util.Locale.ROOT);
        if (low.equals(UNKNOWN_BREED.toLowerCase(java.util.Locale.ROOT))) return true;
        String compact = t.replaceAll("\\s+","");
        return compact.contains("알수없는");
    }

    // === 임계값/상수 ===
    private static final String UNKNOWN_BREED = "알 수 없는 견종";

    // 라벨 필터용: 범주/배경/엉뚱 키워드(부분일치 기준, 공백 무시/소문자 비교)
    private static final java.util.List<String> BANNED_LABELS = java.util.List.of(
            "dog","dogs","mammal","animal","canine","carnivore","carnivores","vertebrate","pet"
    );

    public GoogleVisionResponseDTO analyzeImage(String base64Image) {
        // 1) 이미지 세팅
        GoogleVisionRequestDTO.Request.Image image = new GoogleVisionRequestDTO.Request.Image();
        image.setContent(base64Image);

        // 2) 기존 라벨 감지
        GoogleVisionRequestDTO.Request.Feature labelFeature = new GoogleVisionRequestDTO.Request.Feature();
        labelFeature.setType("LABEL_DETECTION");
        labelFeature.setMaxResults(50); // 넉넉히

        // 3) 객체 로컬라이제이션(마릿수/박스용) 추가
        GoogleVisionRequestDTO.Request.Feature objectFeature = new GoogleVisionRequestDTO.Request.Feature();
        objectFeature.setType("OBJECT_LOCALIZATION");
        // objectFeature는 maxResults 불필요

        // 4) 요청 조립: features에 두 개를 함께 넣기
        GoogleVisionRequestDTO.Request request = new GoogleVisionRequestDTO.Request();
        request.setImage(image);
        request.setFeatures(List.of(labelFeature, objectFeature));

        GoogleVisionRequestDTO body = new GoogleVisionRequestDTO();
        body.setRequests(List.of(request));

        // Vision API 호출
        return googleVisionWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("images:annotate")
                        .queryParam("key", visionApiKey)
                        .build()
                )
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GoogleVisionResponseDTO.class)
                .timeout(java.time.Duration.ofSeconds(6))            // ★ 추가: 6초 타임아웃
                .retryWhen(reactor.util.retry.Retry.fixedDelay(2,    // ★ (선택) 2회 재시도
                                java.time.Duration.ofMillis(200))
                        .filter(ex -> ex instanceof org.springframework.web.reactive.function.client.WebClientRequestException)
                )
                .block(); // 동기 처리
    }

    public VisionAnalysisResult analyzeImage(MultipartFile dogImageFile) {
        try {
            byte[] imageBytes = dogImageFile.getBytes();
            String base64Image = Base64.encodeBase64String(imageBytes);

            GoogleVisionResponseDTO response = analyzeImage(base64Image);

            // --- NPE 가드: 응답/라벨/오브젝트 ---
            var responses = Optional.ofNullable(response.getResponses()).orElse(List.of());
            if (responses.isEmpty()) {
                throw new RuntimeException("Vision 응답 비어있음");
            }
            var res    = responses.get(0);
            var labels = Optional.ofNullable(res.getLabelAnnotations()).orElse(List.of());
            var objs   = Optional.ofNullable(res.getLocalizedObjectAnnotations()).orElse(List.of());

            // --- 개 존재 여부(라벨 기준; 점수 없으면 0으로 간주) ---
            boolean isDogDetected = labels.stream().anyMatch(label -> {
                String d = norm(label.getDescription());                 // 소문자+공백제거
                Float scObj = label.getScore();                          // Vision은 Float/float
                float sc = (scObj == null) ? 0f : scObj;
                return (d.contains("dog") || d.contains("puppy") || d.contains("canine")
                        || d.contains("강아지") || d.contains("반려견"))
                        && sc >= 0.7f;
            });

            // --- 객체 탐지 결과 매핑 ---
            List<VisionAnalysisResult.DetectedObject> mappedObjects = objs.stream()
                    .map(o -> {
                        VisionAnalysisResult.DetectedObject v = new VisionAnalysisResult.DetectedObject();
                        v.setName(o.getName());
                        v.setScore(o.getScore());
                        return v;
                    }).toList();

            // --- 품종 라벨 추출(정규화 + 부분일치 금지어 + 점수 임계) ---
            Optional<GoogleVisionResponseDTO.Response.LabelAnnotation> detectedBreedLabel = labels.stream()
                    // 1) 점수 높은 순으로 정렬
                    .sorted(Comparator.comparing(GoogleVisionResponseDTO.Response.LabelAnnotation::getScore).reversed())
                    .filter(label -> {
                        // 2) 라벨 텍스트 없으면 탈락
                        String desc = label.getDescription();
                        if (desc == null) return false;

                        // 3) 금지어(종/동물/범주성 단어) 포함되면 탈락
                        String descNorm = norm(desc);
                        boolean banned = BANNED_LABELS.stream().anyMatch(descNorm::contains); // 부분일치 차단
                        if (banned) return false;

                        // 4) 점수 임계치(0.80f) 미만이면 탈락
                        Float sObj = label.getScore();
                        float s = (sObj == null) ? 0f : sObj;
                        boolean scoreOk = s >= 0.80f;

                        // 5) 내가 가진 품종 사전(BREED_KOR_MAP)에 존재하는 라벨만 통과
                        boolean inMap = BREED_KOR_MAP.containsKey(desc);

                        // -> 최종 통과 조건!
                        return scoreOk && inMap;
                    })
                    // 6) 위 조건들을 통과한 것 중 ‘가장 점수 높은’ 것 하나만 선택
                    .findFirst();

            String detectedBreed = detectedBreedLabel
                    // ① Vision 영문 라벨을 한글로 매핑(없으면 원문 유지)
                    .map(l -> BREED_KOR_MAP.getOrDefault(l.getDescription(), l.getDescription()))
                    // ② 좌우 공백 제거(“ 말티즈 ” 같은 케이스)
                    .map(String::trim)
                    // ③ 빈 값이면 버리고
                    .filter(s -> !s.isBlank())
                    // ④ 공통 상수로 통일
                    .orElse(UNKNOWN_BREED);

            return VisionAnalysisResult.builder()
                    .detectedBreed(detectedBreed)
                    .labels(labels)
                    .objects(mappedObjects)
                    .isDog(isDogDetected)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("Vision API 분석 중 오류 발생: " + e.getMessage(), e);
        }
    }

}