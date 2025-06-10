package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.domain.User;
import com.example.oyl.dto.UserProfileResponseDTO;
import com.example.oyl.repository.UserRepository;
import com.example.oyl.service.MypageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final MypageService mypageService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfileResponseDTO profile = mypageService.getMyProfile(email);
        return ResponseEntity.ok(
                ApiResponse.<UserProfileResponseDTO>builder()
                        .code("S001")
                        .message("마이페이지 프로필 조회 성공!")
                        .data(profile)                          // 리턴값은 이메일에 해당하는 유저 프로필 전체!
                        .build()
        );
    }
}