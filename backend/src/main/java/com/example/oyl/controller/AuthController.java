package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.LoginResult;
import com.example.oyl.dto.TokenResponseDTO;
import com.example.oyl.dto.UserLoginRequestDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.service.AuthService;
import com.example.oyl.service.RefreshTokenService;
import com.example.oyl.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure}")
    private boolean isSecure;

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody UserLoginRequestDTO requestDTO,
            HttpServletResponse response
    ) {
        LoginResult loginResult = authService.login(requestDTO);

        String refreshToken = loginResult.getRefreshToken();
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .secure(isSecure) // 프로퍼티 값에 따라 자동 설정 (운영에선 true)
                .sameSite(isSecure ? "None" : "Lax") // 보안 설정
                .domain(cookieDomain) // spurfy.site 반영
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code("S002")
                        .message("로그인 성공")
                        .data(loginResult.getAccessToken())
                        .build()
        );
    }

    /* ===================== 리프레시 토큰 재발급 ===================== */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<TokenResponseDTO>builder()
                            .code("A401").message("리프레시 토큰이 없습니다.").data(null).build()
            );
        }

        try {
            String newAccessToken = authService.issueNewAccessToken(refreshToken, response);
            return ResponseEntity.ok(
                    ApiResponse.<TokenResponseDTO>builder()
                            .code("S002").message("토큰 재발급 성공")
                            .data(new TokenResponseDTO(newAccessToken)).build()
            );
        } catch (CustomException e) {
            log.warn("refresh fail code={}, msg={}", e.getErrorCode().getCode(), e.getMessage());
            return ResponseEntity.status(401).body(
                    ApiResponse.<TokenResponseDTO>builder()
                            .code(e.getErrorCode().getCode()).message(e.getMessage()).data(null).build()
            );
        } catch (Exception e) {
            log.error("refresh unexpected err", e);
            return ResponseEntity.status(401).body(
                    ApiResponse.<TokenResponseDTO>builder()
                            .code("A401").message("유효하지 않은 리프레시 토큰 입니다.").data(null).build()
            );
        }
    }

    /* ===================== 로그아웃 ===================== */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // ★ 서비스가 쿠키 추출 + DB revoke + 쿠키 만료까지 전부 수행
        authService.logout(request, response);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S003")
                        .message("로그아웃 완료!")
                        .data(null)
                        .build()
        );
    }

    /* ===================== 헬퍼 ===================== */
    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("refreshToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

}
