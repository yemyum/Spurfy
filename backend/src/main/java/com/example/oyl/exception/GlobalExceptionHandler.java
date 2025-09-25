package com.example.oyl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();

        // 첫 번째 필드 오류 가져오기
        FieldError fieldError = ex.getBindingResult().getFieldError();

        errorResponse.put("code", "V001");
        errorResponse.put("message", fieldError != null ? fieldError.getDefaultMessage() : "유효성 검증 실패");

        return ResponseEntity.badRequest().body(errorResponse); // 400 반환
    }

    // 그 외 500 에러용
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleServerError(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", "S001");
        errorResponse.put("message", "서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", ex.getErrorCode().getCode());
        errorResponse.put("message", ex.getErrorCode().getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
