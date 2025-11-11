package com.example.oyl.service;

import com.example.oyl.domain.User;
import com.example.oyl.dto.PasswordChangeRequestDTO;
import com.example.oyl.dto.UserProfileResponseDTO;
import com.example.oyl.dto.UserUpdateRequestDTO;
import com.example.oyl.dto.WithdrawalRequestDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponseDTO.from(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateMyProfile(String email, UserUpdateRequestDTO updateRequest) {

        // 1. 해당 이메일 사용자를 DB에서 찾아옴
        // 찾을 수 없다면 에러 발생
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. UserUpdateRequestDTO에 담긴 정보로 User 엔티티의 필드를 업데이트
        // 각 필드가 null이 아니거나 비어있지 않은 경우에만 업데이트하도록 조건을 넣어주기

        // 닉네임 중복 검사 로직 추가 (update 전에)
        // updateRequest.getNickname()이 null이 아니고, 공백만 있는 문자열도 아닐 때만 업데이트
        if (updateRequest.getNickname() != null && !updateRequest.getNickname().trim().isEmpty()) {
            // 현재 로그인한 사용자의 기존 닉네임과 다른 경우에만 중복 검사
            if (!user.getNickname().equals(updateRequest.getNickname())) { // 기존 닉네임과 다른 경우에만 검사
                if (userRepository.existsByNickname(updateRequest.getNickname())) {
                    throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
                }
            }
            user.updateNickname(updateRequest.getNickname());
        }

        // 이름 업데이트
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
            user.updateName(updateRequest.getName());
        }

        // 전화번호 업데이트
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().trim().isEmpty()) {
            user.updatePhone(updateRequest.getPhone());
        }

        // ⭐ 이미지 URL 업데이트 (나중에 이미지 업로드 기능 구현 시 여기에 로직 추가) ⭐
        // if (updateRequest.getProfileImageUrl() != null) {
        //     user.updateProfileImage(updateRequest.getProfileImageUrl());
        // }

        // 3. (옵션) 변경된 User 엔티티를 명시적으로 저장할 수 있지만,
        //    @Transactional 어노테이션 덕분에 JPA가 변경된 엔티티를 자동으로 감지(Dirty Checking)하여 데이터베이스에 반영!
        //    따라서 userRepositoy.save(user); 를 따로 호출하지 않아도 ok!

        // 4. 업데이트된 User 엔티티 정보를 UserProfileResponseDTO로 변환하여 반환!
        return UserProfileResponseDTO.from(user);
    }

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public boolean checkNicknameAvailability(String nickname) {
        // 이 닉네임이 DB에 존재하는지 확인. 존재하면 false (사용 불가), 존재하지 않으면 true (사용 가능)
        return !userRepository.existsByNickname(nickname);
    }

    @Override // 인터페이스의 메서드를 오버라이드한다는 의미
    @Transactional // 데이터 변경이므로 @Transactional 필요
    public void changePassword(String email, PasswordChangeRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 2. 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH);
        }

        // 3. 새 비밀번호 암호화 후 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user); // JPA Dirty Checking으로 자동 저장될 수도 있지만 명시적으로 save

        // 비밀번호 변경 성공 후 모든 토큰 철회
        // 사용자의 모든 Refresh Token을 DB에서 무효화시켜서 기존 세션을 끊음
        refreshTokenService.revokeAllTokensForUser(user);
    }

    @Override
    @Transactional
    public void withdrawUser(String email, WithdrawalRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 2. 유저 전체 refresh 토큰 철회
        refreshTokenService.revokeAllTokensForUser(user);

        // 3. 유저 비활성화
        user.deactivate(request.getReason());
        userRepository.save(user);
    }

}
