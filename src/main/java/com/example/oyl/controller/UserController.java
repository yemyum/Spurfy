package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.UserSignupRequestDTO;
import com.example.oyl.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody @Valid UserSignupRequestDTO requestDTO) {
        userService.signup(requestDTO); // 핵심!
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code("S001")
                        .message("회원가입 성공")
                        .data(null)
                        .build()
        );
    }
}
