package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendHistoryResponseDTO {

    private Long id; // 기록의 고유 ID (프론트에서 각 항목을 구분할 때 유용)
    private String imageUrl; // 강아지 이미지 URL (프론트에서 이미지 보여줄 때)
    private String detectedBreed; // 감지된 강아지 품종

    private String prompt; // 사용자가 AI에게 물어본 질문

    private String intro;
    private String compliment;
    private String recommendationHeader;
    private String spaName;
    private List<String> spaDescription; // 여러 줄일 수 있으니 List<String>으로
    private String closing;
    private String spaSlug; // 스파 상세 페이지로 이동할 때 필요한 슬러그

    private LocalDateTime createdAt; // 기록 생성 시간 (언제 추천받았는지 보여줄 때)

    private String errorMessage;

    // 만약 에러 발생 기록도 보여주고 싶다면 추가
    // private String errorMessage;
}
