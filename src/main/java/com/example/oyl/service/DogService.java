package com.example.oyl.service;

import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;

public interface DogService {
    DogResponseDTO registerDog(String userEmail, DogRequestDTO dto);
}
