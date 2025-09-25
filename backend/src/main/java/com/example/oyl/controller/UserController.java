package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.LoginResult;
import com.example.oyl.dto.UserLoginRequestDTO;
import com.example.oyl.dto.UserSignupRequestDTO;
import com.example.oyl.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid UserSignupRequestDTO requestDTO) {
        userService.signup(requestDTO);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("회원가입 성공")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody UserLoginRequestDTO requestDTO,
            HttpServletResponse response
    ) {
        // userService.login()에서 AccessToken, RefreshToken 둘 다 발급
        LoginResult loginResult = userService.login(requestDTO);

        boolean isProd = false; // 로컬 개발중: HTTP라 false

        String refreshToken = loginResult.getRefreshToken();
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/api/users")
                .maxAge(Duration.ofDays(7))
                .secure(isProd ? true : false)
                .sameSite(isProd ? "None" : "Lax")
                // .domain("your-domain.com")
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

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.existsByEmail(email);
        return ResponseEntity.ok(isDuplicate);
    }

}

