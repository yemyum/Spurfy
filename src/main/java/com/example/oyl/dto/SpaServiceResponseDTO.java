package com.example.oyl.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SpaServiceResponseDTO {
    private String serviceId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private Integer price;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> availableTimes;
}
