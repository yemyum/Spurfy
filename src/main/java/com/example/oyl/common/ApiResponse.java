package com.example.oyl.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private String code; // 예: "S001", "V001"
    private String message; // 예: "요청 성공", "유효성 검사 실패"
    private T data; // 응답 데이터 (제네릭)
}
