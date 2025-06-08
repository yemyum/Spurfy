package com.example.oyl.dto;

import lombok.Getter;

@Getter
public class ReviewRequestDTO {
    private String reservationId;
    private String userId;
    private String dogId;
    private int rating;
    private String content;
    private String imageUrl;
}
