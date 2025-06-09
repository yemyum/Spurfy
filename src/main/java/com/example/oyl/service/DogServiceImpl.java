package com.example.oyl.service;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.User;
import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;
import com.example.oyl.dto.DogUpdateRequestDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.DogRepository;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DogServiceImpl implements DogService {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;

    @Override
    public DogResponseDTO registerDog(String userEmail, DogRequestDTO dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Dog dog = Dog.builder()
                .dogId(UUID.randomUUID().toString())
                .user(user)
                .name(dto.getName())
                .breed(dto.getBreed())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .weight(dto.getWeight())
                .notes(dto.getNotes())
                .createdAt(LocalDate.now())
                .build();

        Dog savedDog = dogRepository.save(dog);

        return DogResponseDTO.builder()
                .dogId(savedDog.getDogId())
                .name(savedDog.getName())
                .breed(savedDog.getBreed())
                .birthDate(savedDog.getBirthDate())
                .gender(savedDog.getGender())
                .weight(savedDog.getWeight())
                .build();
    }

    @Override
    public void updateDog(String userEmail, String dogId, DogUpdateRequestDTO dto) {
        // 1. 사용자 찾기 (JWT 기반 이메일)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 강아지 찾기
        Dog dog = dogRepository.findById(dogId)
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

        // 3. 본인 소유 강아지인지 확인
        if (!dog.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_DOG_ACCESS);

        }

        // 4. 정보 업데이트
        dog.updateDog(
                dto.getName(),
                dto.getBreed(),
                dto.getBirthDate(),
                dto.getGender(),
                dto.getWeight(),
                dto.getNotes()
        );
    }

    @Override
    public void deleteDog(String userEmail, String dogId) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 강아지 조회
        Dog dog = dogRepository.findById(dogId)
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

        // 3. 본인 강아지인지 확인
        if (!dog.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_DOG_ACCESS);
        }

        // 4. 삭제
        dogRepository.deleteById(dogId);

    }
}
