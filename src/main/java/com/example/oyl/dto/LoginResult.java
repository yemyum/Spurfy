package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {
    private String accessToken;
    private String refreshToken;
}
