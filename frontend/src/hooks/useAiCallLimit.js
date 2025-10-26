import { useState, useEffect } from 'react';
import api from "../api/axios";

// 횟수는 백엔드와 동일해야 함!
const MAX_DAILY_CALLS = 3;

export const useAiCallLimit = () => {
    const [isLimitExceeded, setIsLimitExceeded] = useState(false);

    // 현재 횟수를 조회하고 상태를 업데이트하는 핵심 함수
    const checkAndUpdateLimit = async () => {
        const token = localStorage.getItem("token");
        if (!token) return;

        try {
            const response = await api.get("/ai-recommendation/call-count");
            const newTodayCount = response.data.data;

            setTodayCount(newTodayCount);

            if (newTodayCount >= MAX_DAILY_CALLS) {
                setIsLimitExceeded(true);
            } else {
                setIsLimitExceeded(false);
            }

            // 업데이트된 횟수 반환!
            return newTodayCount;

        } catch (e) {
            console.error("AI 호출 횟수 업데이트 조회 실패", e);
            return todayCount; // 실패 시 현재 카운트 반환
        }
    };

    // 1. 컴포넌트 마운트 시 초기 상태를 설정 (조회 API 호출)
    useEffect(() => {
        checkAndUpdateLimit();
    }, []);

    // 훅이 반환할 값들
    return {
        isLimitExceeded, // 버튼의 disabled 속성에 연결할 상태
        MAX_DAILY_CALLS, // 에러 메시지에 사용할 상수
        checkAndUpdateLimit, // API 성공/실패 시 수동으로 업데이트할 함수
    };
};