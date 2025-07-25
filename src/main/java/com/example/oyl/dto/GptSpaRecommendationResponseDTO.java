package com.example.oyl.dto;

import lombok.Data;

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
}
