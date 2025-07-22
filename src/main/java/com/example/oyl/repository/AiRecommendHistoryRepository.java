package com.example.oyl.repository;

import com.example.oyl.domain.AiRecommendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AiRecommendHistoryRepository extends JpaRepository<AiRecommendHistory, Long> {

    // 하루 요청 제한 확인용!
    long countByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

}
