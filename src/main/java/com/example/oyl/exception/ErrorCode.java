package com.example.oyl.exception;

public enum ErrorCode {
    USER_NOT_FOUND("U001", "해당 사용자가 존재하지 않습니다."),
    RESERVATION_NOT_FOUND("R001", "예약 정보 없음"),
    INVALID_INPUT("C001", "잘못된 요청입니다."),
    INTERNAL_ERROR("SYS001", "서버 오류가 발생했습니다."),
    SPA_SERVICE_NOT_FOUND("SVC001", "존재하지 않는 스파 서비스 입니다!"),
    UNAUTHORIZED_RESERVATION("R002", "본인의 예약만 접근할 수 있습니다."),
    DOG_NOT_FOUND("D001", "해당 강아지가 존재하지 않습니다."),
    UNAUTHORIZED_DOG("D002", "본인의 강아지만 등록/수정/삭제할 수 있습니다!"),
    DUPLICATE_RESERVATION("R003", "해당 날짜에 이미 예약이 존재합니다!"),
    INVALID_RESERVATION_DATE("R004", "과거 날짜로는 예약할 수 없습니다!"),
    UNAUTHORIZED_DOG_ACCESS("D003", "본인의 강아지만 접근할 수 있습니다."),
    DUPLICATE_PAYMENT("P001", "이미 해당 예약에 대한 결제가 존재합니다."),
    PAYMENT_NOT_FOUND("P002", "해당 예약의 결제 정보가 없습니다."),
    PAYMENT_UNAUTHORIZED("P003", "본인 결제에만 접근/조회/변경할 수 있습니다."),
    ALREADY_PAID("P004", "이미 결제 완료된 예약입니다."),
    REVIEW_NOT_FOUND("RV001", "해당 리뷰가 존재하지 않습니다."),
    CANNOT_CANCEL_COMPLETED_RESERVATION("R005", "이용 완료된 예약은 취소할 수 없습니다."),
    CANNOT_CANCEL_PAST_RESERVATION("R006", "이미 지난 예약은 취소할 수 없습니다."),
    UNAUTHORIZED_REVIEW_ACCESS("RV002", "본인이 작성한 리뷰에만 접근할 수 있습니다."),
    DUPLICATE_REVIEW("RV003", "이미 해당 예약에 대한 리뷰가 작성되었습니다."),
    DUPLICATE_USER_EMAIL("U002", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD("U003", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_NICKNAME("N001", "이미 존재하는 닉네임 입니다."),
    NOT_COMPLETED_RESERVATION("RV004", "이용 완료된 예약만 리뷰 작성이 가능합니다."),
    NEW_PASSWORD_CONFIRM_MISMATCH("U004", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");

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
