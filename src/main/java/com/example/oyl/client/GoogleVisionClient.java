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

    private static final List<String> BANNED_LABELS = List.of(
            "dog", "mammal", "animal", "canine", "carnivores", "vertebrate", "pet"
    );

    private static final String UNKNOWN_BREED = "알 수 없는 견종";

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
                .block(); // 동기 처리
    }

    public VisionAnalysisResult analyzeImage(MultipartFile dogImageFile) {
        try {
            byte[] imageBytes = dogImageFile.getBytes();
            String base64Image = Base64.encodeBase64String(imageBytes);

            GoogleVisionResponseDTO response = analyzeImage(base64Image);

            var res = response.getResponses().get(0);
            var labels = res.getLabelAnnotations();

            boolean isDogDetected = labels.stream().anyMatch(label -> {
                String d = label.getDescription();
                return d != null &&
                        (d.equalsIgnoreCase("dog") || d.equalsIgnoreCase("puppy") || d.equalsIgnoreCase("canine")) &&
                        label.getScore() >= 0.7;
            });

            // 객체 탐지 결과 수집
            List<GoogleVisionResponseDTO.Response.LocalizedObjectAnnotation> objs =
                    Optional.ofNullable(res.getLocalizedObjectAnnotations()).orElse(List.of());

            List<VisionAnalysisResult.DetectedObject> mappedObjects = objs.stream()
                    .map(o -> {
                        VisionAnalysisResult.DetectedObject v = new VisionAnalysisResult.DetectedObject();
                        v.setName(o.getName());
                        v.setScore(o.getScore());
                        return v;
                    }).toList();

            // 품종 라벨 추출
            Optional<GoogleVisionResponseDTO.Response.LabelAnnotation> detectedBreedLabel = labels.stream()
                    .sorted(Comparator.comparing(GoogleVisionResponseDTO.Response.LabelAnnotation::getScore).reversed())
                    .filter(label -> {
                        String desc = label.getDescription();
                        return desc != null &&
                                !BANNED_LABELS.contains(desc.toLowerCase()) &&
                                label.getScore() >= 0.80 &&
                                BREED_KOR_MAP.containsKey(desc);
                    })
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