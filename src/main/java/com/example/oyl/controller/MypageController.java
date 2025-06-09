package com.example.oyl.controller;

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
    public ResponseEntity<UserProfileResponseDTO> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(mypageService.getMyProfile(email));
    }
}