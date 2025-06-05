package com.example.oyl.service;

import com.example.oyl.domain.User;
import com.example.oyl.dto.UserLoginRequestDTO;
import com.example.oyl.dto.UserSignupRequestDTO;
import com.example.oyl.jwt.JwtUtil;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void signup(UserSignupRequestDTO requestDTO) {

        // DTO -> Entity 변환
        User user = User.builder()
                .userId(UUID.randomUUID().toString())  // 고유 ID 자동 생성
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())   // 나중엔 암호화해야 함
                .name(requestDTO.getName())
                .nickname(requestDTO.getNickname())
                .phone(requestDTO.getPhone())
                .profileImage(null)
                .userStatus(1)  // 기본값: 활성
                .userRole(1)    // 기본값: 일반 사용자
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public String login(UserLoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return JwtUtil.createToken(user.getEmail()); // JWT 발급
    }
}
