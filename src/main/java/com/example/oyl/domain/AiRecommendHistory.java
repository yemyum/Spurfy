package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;
import org.apache.jasper.util.UniqueAttributesImpl;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_recommend_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AiRecommendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    private String detectedBreed;

    @Column(nullable = false)
    private Boolean isDog;

    @Column(columnDefinition = "TEXT")
    private String recommendResult;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

    }
 }
