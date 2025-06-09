package com.example.oyl.service;

import com.example.oyl.dto.UserProfileResponseDTO;

public interface MypageService {

    UserProfileResponseDTO getMyProfile(String email);

}
