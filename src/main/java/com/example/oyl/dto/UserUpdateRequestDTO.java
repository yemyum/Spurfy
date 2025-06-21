package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {
    private String nickname;
    private String name;
    private String phone;
    // private String profileImageUrl; // ⭐ 프로필 이미지 수정 기능 추가 시 필요 ⭐

}
