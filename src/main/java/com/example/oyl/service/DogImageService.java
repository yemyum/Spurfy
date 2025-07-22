package com.example.oyl.service;

import com.example.oyl.client.GoogleVisionClient;
import com.example.oyl.client.GptClient;
import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.dto.SpaRecommendationRequestDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DogImageService {

    private final GoogleVisionClient googleVisionClient;
    private final GptClient gptClient;
    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;

    // 하루 최대 AI 호출 횟수 설정
    private static final int MAX_DAILY_AI_CALLS = 5;

    // 강아지 이미지를 분석하고 스파 서비스를 추천하는 핵심 메서드
    public String analyzeAndRecommendSpa(MultipartFile dogImageFile, String userEmail, String question) {

        // 1. 파일이 비어있는지 서비스 계층에서 다시 한번 확인 (컨트롤러에서도 하지만 서비스에서도 중요 로직 시작 전 확인)
        if (dogImageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT); // 파일이 없으면 커스텀 예외 발생!
        }

        // 1) 오늘 날짜의 시작과 끝을 계산 (자정부터 다음날 자정까지)
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // 2) 해당 userEmail이 오늘 요청한 횟수를 DB에서 조회
        long todayCount = aiRecommendHistoryRepository.countByUserIdAndCreatedAtBetween(userEmail, startOfDay, endOfDay);

        // 3) 제한 횟수를 초과했는지 확인
        if (todayCount >= MAX_DAILY_AI_CALLS) {
            System.out.println("AI 대화 횟수 제한 초과! 현재 호출 횟수: " + todayCount + ", 최대 허용: " + MAX_DAILY_AI_CALLS);
            throw new CustomException(ErrorCode.CONVERSATION_LIMIT_EXCEEDED, "하루 AI 대화 횟수(" + MAX_DAILY_AI_CALLS + "회)를 초과했습니다. 내일 다시 시도해주세요.");
        }

        // 2. (선택 사항) 서버의 특정 경로에 이미지 파일 저장
        //    (나중에 구글 비전 AI로 직접 바이트 스트림을 보내는 방식이면 이 부분은 필요 없을 수도 있음!)
        String uploadDir = "uploads"; // 서버 내에 파일을 저장할 디렉토리 이름
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize(); // 절대 경로로 변환
        String savedFileName = null; // 저장된 파일 이름을 담을 변수 추가

        try {
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = dogImageFile.getOriginalFilename();
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
                originalFileName = originalFileName.substring(0, dotIndex);
            }
            // 현재 시간으로 파일명에 유니크한 값 부여
            savedFileName = originalFileName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS")) + fileExtension;

            Path filePath = uploadPath.resolve(savedFileName); // 저장할 파일의 전체 경로

            Files.copy(dogImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("서비스 계층: 파일이 서버에 저장되었습니다 -> " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "파일 저장 중 오류 발생: " + e.getMessage());
        }

        // 3. 구글 비전 AI 연동 로직 (견종 분석 요청)
        String detectedBreed;
        try {
            detectedBreed = googleVisionClient.detectDogBreed(dogImageFile);
            // 여기서 GoogleVisionClient에서 던진 CustomException을 잡을 준비!
        } catch (CustomException e) {
            // 강아지가 아닌 경우나 인식 실패 에러 메시지를 반환하고 여기서 종료
            System.out.println("서비스 계층: 강아지 이미지가 아니거나 인식 실패 -> " + e.getMessage());
            return e.getMessage(); // 컨트롤러로 이 메시지를 바로 전달 (예외 처리 후)
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "견종 분석 실패: " + e.getMessage());
        }
        System.out.println("서비스 계층: 구글 비전 AI 분석 결과 -> " + detectedBreed);

        // 4. GPT 연동 로직 (견종이 인식되었을 때만 실행)
        String spaRecommendation;
        try {
            SpaRecommendationRequestDTO request = new SpaRecommendationRequestDTO(
                    detectedBreed,
                    "성견",  // TODO: 추후 사용자 입력값으로 대체 가능
                    List.of(),  // 피부 타입 선택값
                    List.of(),  // 건강 상태
                    "활발함"  // 활동성
            );

            spaRecommendation = gptClient.recommendSpa(request);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GPT_RECOMMENDATION_FAILED, "스파 추천 실패: " + e.getMessage());
        }
        System.out.println("서비스 계층: GPT 스파 추천 결과 -> " + spaRecommendation);

        try {
            String imageUrlForHistory = "/api/images/" + savedFileName; // 저장된 이미지의 URL (ImageServingController 경로에 맞춰서)
            AiRecommendHistory history = AiRecommendHistory.builder()
                    .userId(userEmail)
                    .imageUrl(imageUrlForHistory) // 저장된 이미지의 URL 기록
                    .detectedBreed(detectedBreed)
                    .isDog(true) // 견종 분석 성공했으니 true
                    .recommendResult(spaRecommendation)
                    .prompt(question)
                    .errorMessage(null) // 성공했으니 에러 없음
                    .build();
            aiRecommendHistoryRepository.save(history); // DB에 저장!
            System.out.println("AI 추천 기록이 DB에 저장되었습니다.");
        } catch (Exception e) {
            // 기록 저장 실패는 전체 프로세스를 막지 않지만, 로그는 남겨야 함.
            System.err.println("AI 추천 기록 저장 중 오류 발생: " + e.getMessage());
            // 필요한 경우 CustomException으로 던져도 됨.
        }

        // 최종 결과를 조합하여 반환
        String imageUrl = "/api/images/" + savedFileName; // ImageServingController의 엔드포인트에 맞춰서 URL 생성
        return "견종: " + detectedBreed + ", 추천 스파: " + spaRecommendation;
    }
}
