package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.UserLoginRequestDTO;
import com.example.oyl.dto.UserSignupRequestDTO;
import com.example.oyl.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<String>> login(@RequestBody UserLoginRequestDTO requestDTO) {
        String token = userService.login(requestDTO);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code("S002")
                        .message("로그인 성공")
                        .data(token)
                        .build()
        );
    }

}

