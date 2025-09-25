package com.example.oyl.dto;

import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import lombok.Data;
import org.apache.coyote.Response;

import java.util.List;

@Data
public class GoogleVisionResponseDTO {
    private List<Response> responses;

    @Data
    public static class Response {
        private List<LabelAnnotation> labelAnnotations;

        private List<LocalizedObjectAnnotation> localizedObjectAnnotations;

        @Data
        public static class LabelAnnotation {
            private String description; // ex. "dog", "mammal", "golden retriever"
            private float score; // 신뢰도

        }

        @Data
        public static class LocalizedObjectAnnotation {
            private String name;   // "Dog" 등
            private Float score;   // 신뢰도 (없을 수도 있음)
            // boundingPoly 필요하면 여기에 정의
        }

    }
}
