package com.example.oyl.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GptSpaRecommendationResponseDTO {
    private String intro; // 첫 번째 줄: 사진 속 아이는 ~
    private String compliment; // 두 번째 줄: 강아지에 대한 칭찬
    private String recommendationHeader; // 세 번째 줄: 이 아이에게 추천하는 스파는:
    private String spaName; // 네 번째 줄: 이모지 + 마크다운 굵게 스파 이름
    private List<String> spaDescription; // 다섯~여섯 번째 줄: 스파 설명 (리스트로 받아서 줄바꿈 처리)
    private String closing; // 마지막 줄: 마무리 멘트
    private String spaSlug; // 스파 상세 페이지 이동을 위한 URL 슬러그
    private Long id; // AI 추천 기록의 고유 ID (저장될 때 부여되는 ID)
    private LocalDateTime createdAt; // AI 추천 기록이 생성된 시간

    private String imageUrl;

    private String errorMessage;
    private String reasonCode;   // (옵션) "MULTI_DOG", "NO_DOG" 등

    public static GptSpaRecommendationResponseDTO createFailureResponse(String errorMessage, String imageUrl) {
        return createFailureResponse(errorMessage, imageUrl, "VISION_RULE");
    }

    public static GptSpaRecommendationResponseDTO createFailureResponse(String errorMessage, String imageUrl, String reasonCode) {
        GptSpaRecommendationResponseDTO res = new GptSpaRecommendationResponseDTO();

        // 성공 시 필드들은 비워두거나 null 처리
        res.setIntro(null);
        res.setCompliment(null);
        res.setRecommendationHeader(null);
        res.setSpaName(null);
        res.setSpaSlug(null);
        res.setSpaDescription(List.of());
        res.setClosing(null);

        // 에러 관련 필드에 값 할당
        res.setErrorMessage(errorMessage);
        res.setImageUrl(imageUrl);
        res.setReasonCode(reasonCode);
        return res;
    }
}
