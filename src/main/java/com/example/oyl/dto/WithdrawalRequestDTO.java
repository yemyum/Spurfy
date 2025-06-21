package com.example.oyl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawalRequestDTO {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password; // 본인 확인을 위해 비밀번호도 함께 받자

    private String reason;

    @NotNull(message = "이용약관 동의가 필요합니다.")
    private Boolean agreeToTerms; // 이용약관 동의 여부
}
