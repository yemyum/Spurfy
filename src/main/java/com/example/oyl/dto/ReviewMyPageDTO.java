package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMyPageDTO {

    private String reviewId;
    private String reservationId;
    private String serviceId;
    private String serviceName;
    private String dogName;
    private String reservationDate; // String 또는 LocalDate로 교체 가능 (프론트 포맷 따라!)
    private int price;
    private int rating;
    private String content;
    private String imageUrl;
    private String createdAt;

}
