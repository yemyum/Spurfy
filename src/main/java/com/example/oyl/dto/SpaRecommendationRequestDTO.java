package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaRecommendationRequestDTO {

    private String breed;              // ex: "푸들"
    private String ageGroup;           // ex: "노령견", "성견", "어린 강아지"
    private List<String> skinTypes;    // ex: ["예민함", "건조함"]
    private List<String> healthIssues; // ex: ["관절 약함", "긴장 잘함"]
    private String activityLevel;      // ex: "활발함", "차분함"
}
