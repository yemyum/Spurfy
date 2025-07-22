package com.example.oyl.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("U001", "해당 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    RESERVATION_NOT_FOUND("R001", "예약 정보 없음", HttpStatus.NOT_FOUND),
    INVALID_INPUT("C001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("SYS001", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SPA_SERVICE_NOT_FOUND("SVC001", "존재하지 않는 스파 서비스 입니다!", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_RESERVATION("R002", "본인의 예약만 접근할 수 있습니다.", HttpStatus.FORBIDDEN),
    DOG_NOT_FOUND("D001", "해당 강아지가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_DOG("D002", "본인의 강아지만 등록/수정/삭제할 수 있습니다!", HttpStatus.FORBIDDEN),
    DUPLICATE_RESERVATION("R003", "해당 날짜에 이미 예약이 존재합니다!", HttpStatus.CONFLICT),
    INVALID_RESERVATION_DATE("R004", "과거 날짜로는 예약할 수 없습니다!", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_DOG_ACCESS("D003", "본인의 강아지만 접근할 수 있습니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_PAYMENT("P001", "이미 해당 예약에 대한 결제가 존재합니다.", HttpStatus.CONFLICT),
    PAYMENT_NOT_FOUND("P002", "해당 예약의 결제 정보가 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_UNAUTHORIZED("P003", "본인 결제에만 접근/조회/변경할 수 있습니다.", HttpStatus.FORBIDDEN),
    ALREADY_PAID("P004", "이미 결제 완료된 예약입니다.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("RV001", "해당 리뷰가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    CANNOT_CANCEL_COMPLETED_RESERVATION("R005", "이용 완료된 예약은 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_PAST_RESERVATION("R006", "이미 지난 예약은 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_REVIEW_ACCESS("RV002", "본인이 작성한 리뷰에만 접근할 수 있습니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_REVIEW("RV003", "이미 해당 예약에 대한 리뷰가 작성되었습니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_EMAIL("U002", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("U003", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    DUPLICATE_NICKNAME("N001", "이미 존재하는 닉네임 입니다.", HttpStatus.CONFLICT),
    NOT_COMPLETED_RESERVATION("RV004", "이용 완료된 예약만 리뷰 작성이 가능합니다.", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_CONFIRM_MISMATCH("U004", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    AI_ANALYSIS_FAILED("AI01", "이미지 분석 AI 서비스 연동에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    GPT_RECOMMENDATION_FAILED("AI02", "GPT 추천 서비스 연동에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    NOT_A_DOG_IMAGE("AI03", "사진에서 강아지를 인식할 수 없습니다. 강아지 사진을 다시 업로드해주세요.", HttpStatus.BAD_REQUEST),
    CONVERSATION_LIMIT_EXCEEDED("E005", "하루 AI 대화 횟수 제한을 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
