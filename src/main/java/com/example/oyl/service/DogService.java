package com.example.oyl.service;

import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;
import com.example.oyl.dto.DogUpdateRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DogService {

    DogResponseDTO registerDog(String userEmail, DogRequestDTO dto, MultipartFile dogImage);

    void updateDog(String userEmail, String dogId, DogUpdateRequestDTO dto, MultipartFile dogImage);

    void deleteDog(String userEmail, String dogId);

    List<DogResponseDTO> getMyDogs(String userEmail);

    DogResponseDTO getDogDetail(String userEmail, String dogId);

}
