package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.domain.User;
import com.example.oyl.dto.PasswordChangeRequestDTO;
import com.example.oyl.dto.UserProfileResponseDTO;
import com.example.oyl.dto.UserUpdateRequestDTO;
import com.example.oyl.dto.WithdrawalRequestDTO;
import com.example.oyl.repository.UserRepository;
import com.example.oyl.service.MypageService;
import com.example.oyl.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class MypageController {

    private final MypageService mypageService;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfileResponseDTO profile = mypageService.getMyProfile(email);
        return ResponseEntity.ok(
                ApiResponse.<UserProfileResponseDTO>builder()
                        .code("S001")
                        .message("마이페이지 프로필 조회 성공!")
                        .data(profile)       // 리턴값은 이메일에 해당하는 유저 프로필 전체!
                        .build()
        );
    }

    @PutMapping("/profile") // PUT 요청을 처리하도록 매핑
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateMyProfile(
            @RequestBody UserUpdateRequestDTO updateRequest
    ) {
        // 현재 로그인된 사용자의 이메일 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfileResponseDTO updatedProfile = mypageService.updateMyProfile(email, updateRequest);

        return ResponseEntity.ok(
                ApiResponse.<UserProfileResponseDTO>builder()
                        .code("S001")
                        .message("프로필 정보가 성공적으로 수정되었습니다.")
                        .data(updatedProfile)
                        .build()
        );
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean available = mypageService.checkNicknameAvailability(nickname);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .code("S001")
                        .message(message)
                        .data(available)
                        .build()
        );
    }

    // 비밀번호 변경
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody PasswordChangeRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        mypageService.changePassword(email, request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("비밀번호가 성공적으로 변경되었습니다.")
                        .build()
        );
    }

    // 회원 탈퇴 엔드포인트
    @DeleteMapping("/withdrawal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(
            @RequestBody @Valid WithdrawalRequestDTO request,
            HttpServletResponse response
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        mypageService.withdrawUser(email, request); // 내부에서 revokeAllTokensForUser 호출

        // 🔥 이 한 줄만 남겨 (서비스에서 쿠키 옵션 동일하게 처리)
        refreshTokenService.expireRefreshCookie(response);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("회원 탈퇴가 성공적으로 처리되었습니다.")
                        .build()
        );
    }
}