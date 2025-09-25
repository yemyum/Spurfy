package com.example.oyl.dto;

import lombok.Data;

@Data
public class PasswordChangeRequestDTO {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
