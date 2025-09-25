package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.TokenResponseDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.service.RefreshTokenService;
import com.example.oyl.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final UserService userService; // 로그인/유저 로직

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
            String newAccessToken = refreshTokenService.issueNewAccessToken(refreshToken, response);
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
        refreshTokenService.logout(request, response);

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
