package com.example.oyl.dto;

import lombok.Getter;

@Getter
public class ReviewUpdateDTO {
    private int rating;
    private String content;
    private String imageUrl;
}
