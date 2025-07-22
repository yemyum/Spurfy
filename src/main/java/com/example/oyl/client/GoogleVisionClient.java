package com.example.oyl.client;

import com.example.oyl.dto.GoogleVisionRequestDTO;
import com.example.oyl.dto.GoogleVisionResponseDTO;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleVisionClient {

    private final WebClient googleVisionWebClient;

    @Value("${google.vision.api-key}")
    private String visionApiKey;

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

    public String detectDogBreed(MultipartFile dogImageFile) {
        try {
            // 1. 이미지 -> Base64 인코딩
            byte[] imageBytes = dogImageFile.getBytes();
            String base64Image = Base64.encodeBase64String(imageBytes);

            // 2. 비전 API 호출 (이미 @Value로 주입받았으니 따로 넘길 필요 없음!)
            GoogleVisionResponseDTO response = analyzeImage(base64Image);

            // 3. 응답에서 견종 추출
            if (response == null || response.getResponses() == null || response.getResponses().isEmpty() ||
                    response.getResponses().get(0).getLabelAnnotations() == null || response.getResponses().get(0).getLabelAnnotations().isEmpty()) {
                throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "사진에 강아지를 인식하지 못했어요! 정면 얼굴이 잘 보이게 다시 찍어주세요!");
            }

            // 2. 라벨 목록 가져오기
            List<GoogleVisionResponseDTO.Response.LabelAnnotation> labels =
                    response.getResponses().get(0).getLabelAnnotations();

            // 3. 강아지 관련 라벨이 있는지 확인하고, 그 중 가장 적합한 견종을 찾기
            boolean isDogDetected = labels.stream()
                    .anyMatch(label -> label.getDescription() != null &&
                            (label.getDescription().equalsIgnoreCase("dog") ||
                                    label.getDescription().equalsIgnoreCase("puppy") ||
                                    label.getDescription().equalsIgnoreCase("canine")) &&
                            label.getScore() > 0.7);  // 신뢰도 70% 이상일 때만 강아지로 인정 (임계값은 조절 가능!)

            if (!isDogDetected) {
                // 직접적인 강아지 키워드가 없으면 강아지가 아니라고 판단 (예: 고양이, 풍경 등)
                throw new CustomException(ErrorCode.INVALID_INPUT, "업로드된 이미지는 강아지 사진이 아닌 것 같습니다.");
            }

            // 보통 첫 번째 라벨이 가장 높은 score를 가지지만, 혹시 모르니 score 기준으로 정렬.
            Optional<GoogleVisionResponseDTO.Response.LabelAnnotation> detectedBreedLabel = labels.stream()
                    .sorted(Comparator.comparing(GoogleVisionResponseDTO.Response.LabelAnnotation::getScore).reversed()) // score 높은 순으로 정렬
                    .filter(label -> label.getDescription() != null &&
                            !label.getDescription().equalsIgnoreCase("dog") && // 'dog' 같은 일반적인 단어 제외
                            !label.getDescription().equalsIgnoreCase("mammal") &&
                            !label.getDescription().equalsIgnoreCase("animal") &&
                            !label.getDescription().equalsIgnoreCase("canine") &&
                            label.getScore() > 0.6)  // 견종 특정 라벨의 신뢰도 (조절 가능)
                    .findFirst();  // 가장 높은 점수의 '구체적인' 견종 라벨 찾기

            if (detectedBreedLabel.isPresent()) {
                return detectedBreedLabel.get().getDescription();
            } else {
                // 'dog'이라고는 인식했지만, 구체적인 견종을 찾지 못한 경우
                // 이 경우에도 GPT에 "강아지"라고라도 보내서 일반적인 스파 추천을 받게 하거나,
                // 아니면 "알 수 없는 견종의 강아지"라고 메시지를 보낼 수 있음
                // 일단 "알 수 없는 견종"으로 처리
                return "알 수 없는 견종의 강아지"; // 또는 "Dog"으로 반환해서 GPT에 넘길 수도 있음
            }

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
        } catch (CustomException e) {
            // 이미 CustomException으로 던져진 예외는 다시 던지기 (서비스 계층에서 잡을 수 있게)
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Vision API 분석 중 예상치 못한 오류 발생: " + e.getMessage(), e);
        }
    }

}

