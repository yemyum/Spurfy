package com.example.oyl.dto;

import lombok.Data;
import java.util.List;

@Data
public class GoogleVisionRequestDTO {
    private List<Request> requests;

    @Data
    public static class Request {
        private Image image;
        private List<Feature> features;

        @Data
        public static class Image {
            private String content; // base64 문자열

        }

        @Data
        public static class Feature {
            private String type = "LABEL_DETECTION";
            private int maxResults = 5;
        }

    }
}
