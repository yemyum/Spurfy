package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.exception.CustomException;
import com.example.oyl.service.DogImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/dog-image")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DogImageUploadController {

    private final DogImageService dogImageService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> uploadDogImages(
            @RequestParam("dogImageFile") MultipartFile dogImageFile,
            @RequestParam("question") String question
    ) {
        // React에서 'dogImageFile'이라는 이름으로 파일을 보낼 거라고 약속!
        // 1. 파일이 비어있는지 먼저 확인! (파일이 없는 요청이 올 수도 있으니까)
        if (dogImageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .code("E001")
                            .message("파일을 찾을 수 없습니다. 파일을 선택해주세요.")
                            .data(null)
                            .build()
            );
        }

        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("로그인한 사용자 이메일: " + userEmail);
            String result = dogImageService.analyzeAndRecommendSpa(dogImageFile, userEmail, question);

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code("S004")
                            .message("강아지 사진 분석 및 스파 추천 완료!")
                            .data(result) // 서비스에서 받은 최종 결과를 데이터로 넘겨줌
                            .build()
            );

        } catch (CustomException e) { // 파일 저장/처리 중 IOException 발생 시
                // e.printStackTrace();
            return ResponseEntity
                    .status(e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.BAD_REQUEST) // CustomException의 getHttpStatus() 사용
                    .body(ApiResponse.<String>builder()
                                .code(e.getErrorCode() != null ? e.getErrorCode().getCode() : "E00X")
                                .message(e.getMessage())
                                .data(null)
                                .build()
            );

        } catch (Exception e) {  // CustomException 외의 모든 예상치 못한 에러를 여기서 잡음
                // e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<String>builder()
                            .code("E999")
                            .message("예상치 못한 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                            .data(null)
                            .build()
            );
        }

    }
}
