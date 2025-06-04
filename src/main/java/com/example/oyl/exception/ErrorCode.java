package com.example.oyl.exception;

public enum ErrorCode {
    USER_NOT_FOUND("U001", "해당 사용자가 존재하지 않습니다."),
    INVALID_INPUT("C001", "잘못된 요청입니다."),
    INTERNAL_ERROR("S001", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
