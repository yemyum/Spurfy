package com.example.oyl.controller;

import com.example.oyl.domain.User;
import com.example.oyl.dto.UserProfileResponseDTO;
import com.example.oyl.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {

    private final UserRepository userRepository;

    @GetMapping("/test")
    public ResponseEntity<String> mypageTest(HttpServletRequest request) {
        String username = (String) request.getAttribute("username"); // JwtFilter에서 넣은 값!
        return ResponseEntity.ok("마이페이지 입장 환영해요, " + username + " 님 🐽💗");
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("사용자 없음"));
        return ResponseEntity.ok(UserProfileResponseDTO.from(user));
    }
}