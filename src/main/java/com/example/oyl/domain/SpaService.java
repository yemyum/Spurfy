package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String serviceId;

    private String name;
    private String description;

    private String slug;

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

    // 다대다 연관관계!
    @ManyToMany
    @JoinTable(
            name = "spa_service_tags",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
}
