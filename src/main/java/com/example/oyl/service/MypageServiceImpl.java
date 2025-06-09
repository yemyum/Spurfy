package com.example.oyl.service;

import com.example.oyl.domain.User;
import com.example.oyl.dto.UserProfileResponseDTO;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponseDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        return UserProfileResponseDTO.from(user);
    }
}
