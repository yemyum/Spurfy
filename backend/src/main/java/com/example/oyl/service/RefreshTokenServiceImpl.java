package com.example.oyl.service;

import com.example.oyl.domain.RefreshToken;
import com.example.oyl.domain.User;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.jwt.JwtUtil;
import com.example.oyl.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    @Value("${app.cookie.secure:true}") private boolean cookieSecure;
    @Value("${app.cookie.domain:}")    private String cookieDomain;
    private String blankToNull(String v){ return (v==null||v.isBlank())?null:v; }

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

    // 재발급: Optional 안 쓰는 현재 시그니처 기준
    @Override
    public String issueNewAccessToken(String refreshToken, HttpServletResponse response) {
        // 0) JWT 자체 검증(서명/만료)
        jwtUtil.validateTokenOrThrow(refreshToken);

        // 1) DB 유효 토큰 조회 (revoked=false && exp>now && hash 일치)
        RefreshToken token = findValidToken(refreshToken);
        if (token == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = token.getUser();

        // 2) 새 Access 발급
        String newAccess = jwtUtil.createAccessToken(user);

        // 3) ★ 회전: 기존 refresh revoke + 새 refresh 발급/저장 + 쿠키 교체
        String newRefresh = jwtUtil.createRefreshToken(user);
        revokeToken(refreshToken);           // 기존 것 무효화
        save(user, newRefresh);              // DB 저장(해시/만료일은 save 내부)

        long maxAge = jwtUtil.getRefreshExpDays() * 24L * 60 * 60; // 초 단위
        setRefreshCookie(response, newRefresh, maxAge);            // 새 쿠키 심기

        return newAccess;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken != null) {
            try { revokeToken(refreshToken); } catch (Exception ignore) {}
        }
        expireRefreshCookie(response); // 브라우저 쿠키 만료
    }

    private void setRefreshCookie(HttpServletResponse resp, String value, long maxAgeSec) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .path("/api/users")
                .maxAge(maxAgeSec);

        if (cookieSecure) {
            b.secure(true).sameSite("None");      // ✅ 배포(HTTPS)
        } else {
            b.secure(false).sameSite("Lax");      // ✅ 로컬(HTTP localhost)
        }

        String domain = blankToNull(cookieDomain);
        if (domain != null) b.domain(domain);

        resp.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public void expireRefreshCookie(HttpServletResponse resp) {
        setRefreshCookie(resp, "", 0);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) return null;
        for (var c : cookies) {
            if ("refreshToken".equals(c.getName())) {
                // 인코딩 안 했으면 아래 try/catch 통과해도 원문 그대로 돌아옴
                try {
                    return java.net.URLDecoder.decode(c.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ignore) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
