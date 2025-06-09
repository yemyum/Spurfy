package com.example.oyl.service;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.User;
import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;
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
                .orElseThrow(() -> new RuntimeException("해당 이메일로 등록된 사용자가 없습니다."));

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
}
