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

    public GoogleVisionResponseDTO analyzeImage(String base64Image) {
        // DTO 생성
        GoogleVisionRequestDTO.Request.Image image = new GoogleVisionRequestDTO.Request.Image();
        image.setContent(base64Image);

        GoogleVisionRequestDTO.Request.Feature feature = new GoogleVisionRequestDTO.Request.Feature();
        feature.setType("LABEL_DETECTION");
        feature.setMaxResults(10);

        GoogleVisionRequestDTO.Request request = new GoogleVisionRequestDTO.Request();
        request.setImage(image);
        request.setFeatures(List.of(feature));

        GoogleVisionRequestDTO body = new GoogleVisionRequestDTO();
        body.setRequests(List.of(request));

        // Vision API 호출
        return googleVisionWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("images:annotate")
                        .queryParam("key", visionApiKey) // 여기에 key 붙이기
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

            if (response == null || response.getResponses() == null || response.getResponses().isEmpty() ||
                    response.getResponses().get(0).getLabelAnnotations() == null || response.getResponses().get(0).getLabelAnnotations().isEmpty()) {
                throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "사진에 강아지를 인식하지 못했어요! 정면 얼굴이 잘 보이게 다시 찍어주세요!");
            }

            List<GoogleVisionResponseDTO.Response.LabelAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

            boolean isDogDetected = labels.stream()
                    .anyMatch(label -> {
                        String desc = label.getDescription();
                        return desc != null &&
                                (desc.equalsIgnoreCase("dog") || desc.equalsIgnoreCase("puppy") || desc.equalsIgnoreCase("canine")) &&
                                label.getScore() > 0.7;
                    });

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
                    .map(label -> BREED_KOR_MAP.getOrDefault(label.getDescription(), label.getDescription()))
                    .orElse("알 수 없는 견종의 강아지");

            return VisionAnalysisResult.builder()
                    .detectedBreed(detectedBreed)
                    .labels(labels)
                    .isDog(isDogDetected)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Vision API 분석 중 오류 발생: " + e.getMessage(), e);
        }
    }

}