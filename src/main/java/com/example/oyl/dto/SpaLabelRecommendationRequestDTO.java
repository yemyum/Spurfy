package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaLabelRecommendationRequestDTO {
    private List<String> labels; // 비전 API 라벨 값들
    private String breed;
    private String ageGroup;
    private List<String> skinTypes;
    private List<String> healthIssues;
    private String activityLevel;

    private String checklist;
    private String question;

    private String selectedBreed;
}
