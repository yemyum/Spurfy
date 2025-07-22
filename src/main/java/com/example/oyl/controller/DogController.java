package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.DogRequestDTO;
import com.example.oyl.dto.DogResponseDTO;
import com.example.oyl.dto.DogUpdateRequestDTO;
import com.example.oyl.service.DogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dogs")
@PreAuthorize("isAuthenticated()")
public class DogController {

    private final DogService dogService;

    @PostMapping
    public ResponseEntity<ApiResponse<DogResponseDTO>> registerDog (@RequestBody DogRequestDTO dto) {
        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        DogResponseDTO response = dogService.registerDog(userEmail, dto);

        return ResponseEntity.ok(
                ApiResponse.<DogResponseDTO>builder()
                        .code("S001")
                        .message("강아지 등록 성공!")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{dogId}")
    public ResponseEntity<?> updateDog(
            @PathVariable String dogId,
            @RequestBody DogUpdateRequestDTO dto
            ) {

        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        dogService.updateDog(userEmail, dogId, dto);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("강아지 정보가 수정되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/{dogId}")
    public ResponseEntity<?> deleteDog(@PathVariable String dogId) {
        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        dogService.deleteDog(userEmail, dogId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("강아지가 삭제되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DogResponseDTO>>> getMyDogs() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<DogResponseDTO> dogs = dogService.getMyDogs(email);

        return ResponseEntity.ok(
                ApiResponse.<List<DogResponseDTO>>builder()
                        .code("S001")
                        .message("강아지 목록 조회 성공")
                        .data(dogs)
                        .build()
        );
    }

    @GetMapping("/{dogId}")
    public ResponseEntity<ApiResponse<DogResponseDTO>> getDogDetail(@PathVariable String dogId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        DogResponseDTO dog = dogService.getDogDetail(userEmail, dogId);

        return ResponseEntity.ok(
                ApiResponse.<DogResponseDTO>builder()
                        .code("S001")
                        .message("강아지 상세 조회 성공!")
                        .data(dog)
                        .build()
        );
    }

}
