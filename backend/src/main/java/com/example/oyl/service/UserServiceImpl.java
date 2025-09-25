package com.example.oyl.service;

import com.example.oyl.domain.User;
import com.example.oyl.domain.UserRole;
import com.example.oyl.domain.UserStatus;
import com.example.oyl.dto.LoginResult;
import com.example.oyl.dto.UserLoginRequestDTO;
import com.example.oyl.dto.UserSignupRequestDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.jwt.JwtUtil;
import com.example.oyl.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder; // 의존성 주입
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void signup(UserSignupRequestDTO requestDTO) {

        // 중복 이메일 체크
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_USER_EMAIL);
        }

        // 중복 닉네임 체크
        if (userRepository.existsByNickname(requestDTO.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(requestDTO.getPassword());

        // DTO -> Entity 변환
        User user = User.builder()
                .userId(UUID.randomUUID().toString())  // 고유 ID 자동 생성
                .email(requestDTO.getEmail())
                .password(encodedPw)    // 암호화!
                .name(requestDTO.getName())
                .nickname(requestDTO.getNickname())
                .phone(requestDTO.getPhone())
                .profileImage(null)
                .userStatus(UserStatus.ACTIVE)  // 기본값: 활성
                .userRole(UserRole.USER)    // 기본값: 일반 사용자
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public LoginResult login(UserLoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // Access / Refresh 발급
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        // refreshToken 저장 (expiresAt 계산은 서비스가 알아서 함)
        refreshTokenService.save(user, refreshToken);

        return new LoginResult(accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용 작업이므로 readOnly = true
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
