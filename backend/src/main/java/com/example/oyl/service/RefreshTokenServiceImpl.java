package com.example.oyl.service;

import com.example.oyl.domain.RefreshToken;
import com.example.oyl.domain.User;
import com.example.oyl.jwt.JwtUtil;
import com.example.oyl.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // 토큰 원문 해시 처리 (보안 강화)
    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // 1. 리프레시 토큰 저장
    @Transactional
    @Override
    public RefreshToken save(User user, String refreshToken) {
        log.info(">>> save() 들어옴 refreshToken={}", refreshToken);
        String hash = sha256(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtUtil.getRefreshExpDays());

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(expiresAt)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
        return refreshTokenRepository.save(token);
    }

    // 2. 유효 토큰 조회
    @Transactional(readOnly = true)
    @Override
    public RefreshToken findValidToken(String refreshToken) {
        String hash = sha256(refreshToken);
        return refreshTokenRepository
                .findFirstByTokenHashAndRevokedFalseAndExpiresAtAfter(hash, LocalDateTime.now())
                .orElse(null);
    }

    // 3. 단일 토큰 철회: update 쿼리로 한 방에
    @Transactional
    @Override
    public void revokeToken(String refreshToken) {
        String hash = sha256(refreshToken);
        refreshTokenRepository.revokeByHash(hash);
    }

    // 4. 유저 전체 토큰 철회: update 쿼리로 한 방에
    @Transactional
    @Override
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }

    // 5. DB에 남은 만료된/오래된 토큰 정리
    @Transactional
    @Override
    public void cleanUpExpiredTokens() {
        refreshTokenRepository.deleteRevokedOrExpired(LocalDateTime.now());
    }

}
