package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "spa_services")
public class SpaService {

    @Id
    @Column(name = "service_id")
    private String serviceId; // ex: spa_001

    private String name;
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    private Long price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "available_times")
    private String availableTimes; // "10:00,12:00,14:00" 형태
}
