package com.example.oyl.controller;

import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;
import com.example.oyl.service.DogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dogs")
public class DogController {

    private final DogService dogService;

    @PostMapping
    public ResponseEntity<DogResponseDTO> registerDog (@RequestBody DogRequestDTO dto) {

        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        DogResponseDTO response = dogService.registerDog(userEmail, dto);
        return ResponseEntity.ok(response);
    }
}
