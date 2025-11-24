package com.example.oyl.service;

import com.example.oyl.domain.RefreshToken;
import com.example.oyl.domain.User;
import com.example.oyl.dto.LoginResult;
import com.example.oyl.dto.UserLoginRequestDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.jwt.JwtUtil;
import com.example.oyl.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // 사용자 조회에 필요
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService; // 토큰 관리에 필요

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;
    @Value("${app.cookie.domain:}")
    private String cookieDomain;
    private String blankToNull(String v) {
        return (v==null||v.isBlank())?null:v;
    }

    @Override
    @Transactional
    public LoginResult login(UserLoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // Access / Refresh 발급
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        // refreshToken 저장
        refreshTokenService.save(user, refreshToken);

        return new LoginResult(accessToken, refreshToken);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken != null) {
            try { refreshTokenService.revokeToken(refreshToken); } catch (Exception ignore) {}
        }
        expireRefreshCookie(response); // 브라우저 쿠키 만료
    }

    // 재발급: Optional 안 쓰는 현재 시그니처 기준
    @Override
    public String issueNewAccessToken(String refreshToken, HttpServletResponse response) {
        // 0) JWT 자체 검증(서명/만료)
        jwtUtil.validateTokenOrThrow(refreshToken);

        // 1) DB 유효 토큰 조회 (revoked=false && exp>now && hash 일치)
        RefreshToken token = refreshTokenService.findValidToken(refreshToken);
        if (token == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = token.getUser();

        // 2) 새 Access 발급
        String newAccess = jwtUtil.createAccessToken(user);

        // 3) ★ 회전: 기존 refresh revoke + 새 refresh 발급/저장 + 쿠키 교체
        String newRefresh = jwtUtil.createRefreshToken(user);
        refreshTokenService.revokeToken(refreshToken);           // 기존 것 무효화
        refreshTokenService.save(user, newRefresh);              // DB 저장(해시/만료일은 save 내부)

        long maxAge = jwtUtil.getRefreshExpDays() * 24L * 60 * 60; // 초 단위
        setRefreshCookie(response, newRefresh, maxAge);            // 새 쿠키 심기

        return newAccess;
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

}
