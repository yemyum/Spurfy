package com.example.oyl.dto;

import lombok.Data;
import org.apache.coyote.Response;

import java.util.List;

@Data
public class GoogleVisionResponseDTO {
    private List<Response> responses;

    @Data
    public static class Response {
        private List<LabelAnnotation> labelAnnotations;

        @Data
        public static class LabelAnnotation {
            private String description; // ex. "dog", "mammal", "golden retriever"
            private float score; // 신뢰도

        }

    }
}
