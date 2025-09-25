package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPublicDTO {

    private String reviewId;
    private String userNickname;
    private String dogName;
    private int rating;
    private String content;
    private String imageUrl;
    private String createdAt; // 프론트에서 포맷 쉽게 하려고 String으로!

}
