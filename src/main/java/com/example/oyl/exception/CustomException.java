package com.example.oyl.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{

    private final ErrorCode errorCode;

    // 1. ErrorCode만 받는 생성자 (기존)
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 2. ErrorCode와 String 메시지를 받는 생성자 (이걸 추가해야 에러 해결)
    public CustomException(ErrorCode errorCode, String message) {
        super(message); // 직접 넘긴 message를 Exception의 메시지로 사용
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

}
