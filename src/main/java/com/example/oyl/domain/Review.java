package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @Column(name = "review_id")
    private String reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id")
    private Dog dog;

    private int rating;

    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_blinded")
    private boolean isBlinded;  // 관리자용

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateReview(int rating, String content, String imageUrl, LocalDateTime updatedAt) {
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = updatedAt;
    }

}
