package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.dto.GptSpaRecommendationResponseDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.service.DogImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/dog-image")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiSpaRecommendationController {

    private final DogImageService dogImageService;

    @PostMapping
    public ResponseEntity<ApiResponse<GptSpaRecommendationResponseDTO>> uploadDogImages(
            @RequestParam("dogImageFile") MultipartFile dogImageFile,
            @RequestParam(value = "checklist", required = false) String checklist,
            @RequestParam(value = "question", required = false) String question
    ) {
        log.info("[Controller] checklist raw: {}", checklist);
        // React에서 'dogImageFile'이라는 이름으로 파일을 보낼 거라고 약속!
        // 1. 파일이 비어있는지 먼저 확인! (파일이 없는 요청이 올 수도 있으니까)
        if (dogImageFile == null || dogImageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<GptSpaRecommendationResponseDTO>builder()
                            .code("E001")
                            .message("사진은 필수입니다. 파일을 선택해주세요!")
                            .data(null)
                            .build()
            );
        }
        // ★ 추가: 이미지 MIME 가드
        String ct = dogImageFile.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<GptSpaRecommendationResponseDTO>builder()
                            .code("E002")
                            .message("이미지 파일만 업로드해주세요!")
                            .data(null)
                            .build()
            );
        }

        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            GptSpaRecommendationResponseDTO result = dogImageService.analyzeAndRecommendSpa(dogImageFile, userEmail, checklist, question);

            // 정상 추천일 때만 성공 메시지
            return ResponseEntity.ok(
                    ApiResponse.<GptSpaRecommendationResponseDTO>builder()
                            .code("S004")
                            .message("강아지 사진 분석 및 스파 추천 완료!")
                            .data(result) // 서비스에서 받은 최종 결과를 데이터로 넘겨줌
                            .build()
            );

        } catch (CustomException e) { // 파일 저장/처리 중 IOException 발생 시
                e.printStackTrace();
            return ResponseEntity
                    .status(e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.BAD_REQUEST) // CustomException의 getHttpStatus() 사용
                    .body(ApiResponse.<GptSpaRecommendationResponseDTO>builder()
                                .code(e.getErrorCode() != null ? e.getErrorCode().getCode() : "E00X")
                                .message(e.getMessage())
                                .data(null)
                                .build()
            );

        } catch (Exception e) {  // CustomException 외의 모든 예상치 못한 에러를 여기서 잡음
                e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<GptSpaRecommendationResponseDTO>builder()
                            .code("E999")
                            .message("예상치 못한 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                            .data(null)
                            .build()
            );
        }

    }
}
