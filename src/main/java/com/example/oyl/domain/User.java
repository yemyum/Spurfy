package com.example.oyl.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "user_status", nullable = false)
    @Enumerated(EnumType.STRING) // 'ACTIVE'/'DEACTIVATED'
    private UserStatus userStatus;

    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    private LocalDateTime updatedAt;

    private String withdrawalReason; // 탈퇴 사유를 저장
    private LocalDateTime withdrawalDate; // 탈퇴 시점을 저장\

    // 닉네임 업데이트
    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
    }

    // 이름 업데이트
    public void updateName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
    }

    // 전화번호 업데이트
    public void updatePhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
    }

    // ⭐ 프로필 이미지 URL 업데이트 (추후 이미지 업로드 기능 구현 시 사용) ⭐
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
    }

    // ⭐ 비밀번호 업데이트 (비밀번호 변경 시 사용) ⭐
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
    }

    // ⭐⭐ 회원 탈퇴 (userStatus 변경) 메서드 추가 ⭐⭐
    public void deactivate(String withdrawalReason) {
        this.userStatus = UserStatus.DEACTIVATED; // 0으로 설정하여 '탈퇴' 상태로 변경
        this.withdrawalReason = withdrawalReason;
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
        this.withdrawalDate = LocalDateTime.now();
    }

    // ⭐ (선택 사항) 계정 활성 상태 확인 메서드 ⭐
    public boolean isActive() {
        return this.userStatus == UserStatus.ACTIVE;
    }

}
