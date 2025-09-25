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

    // imageUrl도 받도록 함수를 수정
    public static GptSpaRecommendationResponseDTO createFailureResponse(String errorMessage, String imageUrl) {
        GptSpaRecommendationResponseDTO res = new GptSpaRecommendationResponseDTO();
        res.setIntro(null);
        res.setCompliment(null);
        // 에러 메시지는 'errorMessage'에 담아서 보냄
        res.setErrorMessage(errorMessage);
        res.setImageUrl(imageUrl); // imageUrl을 DTO에 포함
        res.setReasonCode("VISION_RULE");
        return res;
    }
}
