package com.example.oyl.controller;

import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;
import com.example.oyl.dto.DogUpdateRequestDTO;
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

    @PatchMapping("/{dogId}")
    public ResponseEntity<?> updateDog(
            @PathVariable String dogId,
            @RequestBody DogUpdateRequestDTO dto
            ) {

        try {
            String userEmail = SecurityContextHolder.getContext()
                    .getAuthentication().getName(); // JWT 기반 인증 사용자

            dogService.updateDog(userEmail, dogId, dto);
            return ResponseEntity.ok("강아지 정보가 수정되었습니다.");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{dogId}")
    public ResponseEntity<?> deleteDog(@PathVariable String dogId) {
        try {
            String useEmail = SecurityContextHolder.getContext()
                    .getAuthentication().getName();

                    dogService.deleteDog(useEmail, dogId);
                    return ResponseEntity.ok("강아지가 삭제되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
