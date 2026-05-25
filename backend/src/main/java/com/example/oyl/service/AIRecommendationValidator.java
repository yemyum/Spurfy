package com.example.oyl.service;

import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationValidator {

    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;

    private static final int MAX_DAILY_REQUESTS = 5; // 하루 최대 5번 제한

    // ✅ 호출 제한 검증 로직
    public void validateDailyLimit(String userEmail) {
        int todayCount = getTodayAiCallCount(userEmail);

        log.info("[Validator] 유저 '{}'의 오늘 성공한 AI 호출 횟수: {}/{}", userEmail, todayCount, MAX_DAILY_REQUESTS);

        log.info("[Validator] 유저 '{}'의 오늘 AI 호출 횟수: {}/{}", userEmail, todayCount, MAX_DAILY_REQUESTS);

        if (todayCount >= MAX_DAILY_REQUESTS) {
            log.warn("[Validator] AI 대화 횟수 제한 초과! 유저: {}, 호출 횟수: {}", userEmail, todayCount);
            throw new CustomException(
                    ErrorCode.CONVERSATION_LIMIT_EXCEEDED,
                    "오늘 이용 가능한 AI 추천 횟수(\" + MAX_DAILY_AI_CALLS + \"회)를 모두 초과했습니다. 내일 다시 시도해 주세요!"
            );
        }
    }

    // 💡 화면에 남은 횟수 보여주기 위해 카운트 뱉는 메서드
    public int getTodayAiCallCount(String userEmail) {
        List<LocalDateTime> range = getTodayRange();
        long todayCount = aiRecommendHistoryRepository.countByUserIdAndCreatedAtBetweenAndErrorMessageIsNull(
                userEmail, range.get(0), range.get(1)
        );
        return (int) todayCount;
    }

    // 💡 날짜 계산 공통 메서드
    private List<LocalDateTime> getTodayRange() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        return List.of(startOfDay, endOfDay);
    }

}
