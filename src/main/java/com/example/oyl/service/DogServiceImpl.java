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
import java.util.List;
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

        String cleanNotes = dto.getNotes() == null || dto.getNotes().trim().isEmpty()
                ? null
                : dto.getNotes().trim();

        Dog dog = Dog.builder()
                .dogId(UUID.randomUUID().toString())
                .user(user)
                .name(dto.getName())
                .breed(dto.getBreed())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .weight(dto.getWeight())
                .notes(cleanNotes)
                .createdAt(LocalDate.now())
                .build();

        Dog savedDog = dogRepository.save(dog);

        return DogResponseDTO.builder()
                .dogId(savedDog.getDogId())
                .name(savedDog.getName())
                .breed(savedDog.getBreed())
                .birthDate(savedDog.getBirthDate().toString())
                .gender(savedDog.getGender())
                .weight(savedDog.getWeight())
                .notes(savedDog.getNotes())
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

        dogRepository.save(dog);

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

    @Override
    public List<DogResponseDTO> getMyDogs(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Dog> dogs = dogRepository.findByUser_UserId(user.getUserId());

        return dogs.stream().map(dog ->
                DogResponseDTO.builder()
                        .dogId(dog.getDogId())
                        .name(dog.getName())
                        .breed(dog.getBreed())
                        .birthDate(dog.getBirthDate().toString())
                        .gender(dog.getGender())
                        .weight(dog.getWeight())
                        .notes(dog.getNotes())
                        .build()
        ).toList();
    }

    @Override
    public DogResponseDTO getDogDetail(String userEmail, String dogId) {
        Dog dog = dogRepository.findByDogIdAndUserEmail(dogId, userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

        return DogResponseDTO.fromEntity(dog);
    }

}
