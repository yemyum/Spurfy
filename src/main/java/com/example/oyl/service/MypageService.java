package com.example.oyl.service;

import com.example.oyl.dto.PasswordChangeRequestDTO;
import com.example.oyl.dto.UserProfileResponseDTO;
import com.example.oyl.dto.UserUpdateRequestDTO;
import com.example.oyl.dto.WithdrawalRequestDTO;

public interface MypageService {

    UserProfileResponseDTO getMyProfile(String email);

    UserProfileResponseDTO updateMyProfile(String email, UserUpdateRequestDTO updateRequest);

    boolean checkNicknameAvailability(String nickname);

    void changePassword(String email, PasswordChangeRequestDTO request);

    void withdrawUser(String email, WithdrawalRequestDTO request);

}
