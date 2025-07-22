package com.example.oyl.client;

import com.example.oyl.dto.GoogleVisionRequestDTO;
import com.example.oyl.dto.GoogleVisionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleVisionClient {

    private final WebClient googleVisionWebClient;

    @Value("${spring.cloud.gcp.credentials.location}")
    private String gcpCredentialsPath;

    public GoogleVisionResponseDTO analyzeImage(String base64Image, String apiKey) {
        // DTO 생성
        GoogleVisionRequestDTO.Request.Image image = new GoogleVisionRequestDTO.Request.Image();
        image.setContent(base64Image);

        GoogleVisionRequestDTO.Request.Feature feature = new GoogleVisionRequestDTO.Request.Feature();

        GoogleVisionRequestDTO.Request request = new GoogleVisionRequestDTO.Request();
        request.setImage(image);
        request.setFeatures(List.of(feature));

        GoogleVisionRequestDTO body = new GoogleVisionRequestDTO();
        body.setRequests(List.of(request));

        // Vision API 호출
        return googleVisionWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey) // 여기에 key 붙이기
                        .build()
                )
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GoogleVisionResponseDTO.class)
                .block(); // 동기 처리
    }
}

