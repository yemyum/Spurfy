package com.example.oyl.dto;

import com.example.oyl.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDTO {
    private String email;
    private String nickname;
    private String name;
    private String phone;

    public static UserProfileResponseDTO from(User user) {
        return UserProfileResponseDTO.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .phone(user.getPhone())
                .build();
    }
}
