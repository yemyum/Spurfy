package com.example.oyl.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SpaServiceDTO {
    private String serviceId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private Integer price;
    private boolean isActive; // 직관적인 true/false로 변경
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> availableTimes;

}
