package com.example.oyl.domain;


import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "user_id", nullable = false, length = 50)
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
    private int userStatus;  // 1: 활성, 0: 탈퇴

    @Column(name = "user_role", nullable = false)
    private int userRole;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ⭐⭐ @PrePersist 메서드 추가 ⭐⭐
    // 엔티티가 영속화(DB에 저장)되기 전에 실행되는 메서드
    @PrePersist
    public void prePersist() {
        // userStatus가 아직 설정되지 않았다면 기본값 1 (활성)으로 설정
        if (this.userStatus == 0) { // 0이 초기값이라면 (int의 기본값)
            this.userStatus = 1;
        }
        // createdAt이 아직 설정되지 않았다면 현재 시간으로 설정
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        // updatedAt은 처음 생성될 때 createdAt과 동일하게 설정하는 경우가 많음
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    // ⭐⭐ 프로필 정보 업데이트를 위한 메서드 추가 ⭐⭐

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
    public void deactivate() {
        this.userStatus = 0; // 0으로 설정하여 '탈퇴' 상태로 변경
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
    }

    // ⭐ (선택 사항) 계정 활성 상태 확인 메서드 ⭐
    public boolean isActive() {
        return this.userStatus == 1;
    }

}
