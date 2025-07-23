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
public class VisionAnalysisResult {
    private String detectedBreed;  // ex: "푸들", "알 수 없는 견종의 강아지"
    private List<GoogleVisionResponseDTO.Response.LabelAnnotation> labels;  // GoogleVision 라벨 객체 리스트
    private boolean isDog;
}
