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

        boolean isProd = true; // TODO: 프로필 보고 결정

        String encoded = URLEncoder.encode(loginResult.getRefreshToken(), StandardCharsets.UTF_8);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", encoded)
                .httpOnly(true)
                .secure(isProd)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("None")
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

