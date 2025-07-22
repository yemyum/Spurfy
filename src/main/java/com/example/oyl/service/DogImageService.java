package com.example.oyl.service;

import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class DogImageService {

    // (TODO: 실제 구글 비전 API, GPT API 연동 시 사용할 클라이언트 객체들 선언)
    // private final GoogleVisionApiClient googleVisionApiClient;
    // private final OpenAIApiClient openAIApiClient;

    // 강아지 이미지를 분석하고 스파 서비스를 추천하는 핵심 메서드
    public String analyzeAndRecommendSpa(MultipartFile dogImageFile) {

        // 1. 파일이 비어있는지 서비스 계층에서 다시 한번 확인 (컨트롤러에서도 하지만 서비스에서도 중요 로직 시작 전 확인)
        if (dogImageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT); // 파일이 없으면 커스텀 예외 발생!
        }

        // 2. (선택 사항) 서버의 특정 경로에 이미지 파일 저장
        //    (나중에 구글 비전 AI로 직접 바이트 스트림을 보내는 방식이면 이 부분은 필요 없을 수도 있음!)
        String uploadDir = "uploads"; // 서버 내에 파일을 저장할 디렉토리 이름
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize(); // 절대 경로로 변환

        try {
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = dogImageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName); // 저장할 파일의 전체 경로

            // 파일 복사 (실제 서버에 저장)
            Files.copy(dogImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("서비스 계층: 파일이 서버에 저장되었습니다 -> " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "파일 저장 중 오류 발생: " + e.getMessage());
        }


        // 3. TODO: 구글 비전 AI 연동 로직 (견종 분석 요청)
        String detectedBreed = "분석된 견종: 비글"; // 나중에는 구글 비전 AI가 실제 견종을 알려줄 예정!
        System.out.println("서비스 계층: 구글 비전 AI 분석 결과 -> " + detectedBreed);
        // if (ai_error_condition) { throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED); }


        // 4. TODO: GPT 연동 로직 (스파 서비스 추천 요청)
        String spaRecommendation = "비글에게는 활력 스파와 털 관리 스파를 추천합니다."; // 나중에는 GPT가 실제 추천을 해줄 예정
        System.out.println("서비스 계층: GPT 스파 추천 결과 -> " + spaRecommendation);
        // if (gpt_error_condition) { throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED); }

        // 최종 결과를 조합하여 반환
        return "견종: " + detectedBreed + ", 추천 스파: " + spaRecommendation;
    }
}
