package com.example.oyl.service;

import com.example.oyl.dto.LoginResult;
import com.example.oyl.dto.UserLoginRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResult login(UserLoginRequestDTO requestDTO);

    void logout(HttpServletRequest request, HttpServletResponse response);

    // 새 AccessToken 발급
    String issueNewAccessToken(String refreshToken, HttpServletResponse response);

    void expireRefreshCookie(HttpServletResponse response);

}
