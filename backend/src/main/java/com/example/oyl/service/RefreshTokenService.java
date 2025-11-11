package com.example.oyl.service;

import com.example.oyl.domain.RefreshToken;
import com.example.oyl.domain.User;
import com.example.oyl.dto.LoginResult;
import com.example.oyl.dto.UserLoginRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

public interface RefreshTokenService {
    // refreshToken 원문 받아서 내부에서 hash 처리
    RefreshToken save(User user, String refreshToken);

    // 원문 refreshToken으로 유효성 체크 후 DB에서 찾아 반환
    RefreshToken findValidToken(String refreshToken);

    // 단일 토큰 철회
    void revokeToken(String refreshToken);

    // 유저 전체 토큰 철회
    void revokeAllTokensForUser(User user);

    // 만료/폐기된 토큰 청소
    void cleanUpExpiredTokens();
}
