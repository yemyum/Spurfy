package com.example.oyl.repository;

import com.example.oyl.domain.AiRecommendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AiRecommendHistoryRepository extends JpaRepository<AiRecommendHistory, Long> {

    // 하루 요청 제한 확인용!
    long countByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

    // AI 추천 기록들 가져오기
    List<AiRecommendHistory> findByUserId(String userId);

    // DB 정리용
    List<AiRecommendHistory> findByImageUrl(String imageUrl);

}
