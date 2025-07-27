package com.example.oyl.scheduler;

import com.example.oyl.domain.AiRecommendHistory;
import com.example.oyl.repository.AiRecommendHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Slf4j
@Component
public class ImageCleanupScheduler {

    private final Path uploadPath; // 실제 파일 경로를 나타내는 Path 객체
    private final Duration imageRetentionDuration;
    private final AiRecommendHistoryRepository aiRecommendHistoryRepository;
    private final String imageApiBaseUrl;

    // 생성자에서 값 세팅!
    public ImageCleanupScheduler(
            @Value("${app.image.retention-hours:1h}") String retentionDurationRaw, // String으로 받아야 함!!
            @Value("${file.upload-dir:uploads}") String uploadDirRaw,
            @Value("${app.image.api-base-url:/api/images/}") String imageApiBaseUrl,
            AiRecommendHistoryRepository aiRecommendHistoryRepository
    ) throws IOException {
        // String → Duration으로 직접 변환
        this.imageRetentionDuration = parseDuration(retentionDurationRaw);
        this.uploadPath = Paths.get(uploadDirRaw).toAbsolutePath().normalize();
        this.aiRecommendHistoryRepository = aiRecommendHistoryRepository; // 주입받은 객체 저장
        this.imageApiBaseUrl = imageApiBaseUrl; // 주입받은 객체 저장

        if (!Files.exists(this.uploadPath)) {
            Files.createDirectories(this.uploadPath);
            log.info("디렉토리 생성됨: {}", this.uploadPath);
        }
    }

    // 스케줄러: 주기적으로 파일 삭제
    @Scheduled(fixedRateString = "${app.image.cleanup-interval-ms:3600000}")
    public void cleanupOldImages() {
        log.info("⏰ 이미지 정리 스케줄러 시작...");
        LocalDateTime now = LocalDateTime.now();
        // 람다에서 수정 가능한 변수를 위해 AtomicLong 사용!
        AtomicLong filesDeletedCounter = new AtomicLong(0);
        AtomicLong dbUpdatedCounter = new AtomicLong(0);

        try (Stream<Path> walk = Files.walk(uploadPath)) {
                    walk.filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
                            return Duration.between(lastModifiedTime, now).compareTo(imageRetentionDuration) > 0;
                        } catch (IOException e) {
                            log.error("파일 시간 확인 중 오류: {} - {}", path, e.getMessage());
                            return false;
                        }
                    })
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String imageUrlInDb = imageApiBaseUrl + fileName;

                        try {
                            Files.delete(path);
                            filesDeletedCounter.incrementAndGet(); // AtomicLong 값 증가
                            log.info("🗑️ 오래된 이미지 파일 삭제됨: {}", path.getFileName());

                            // 2. DB에서 해당 imageUrl을 가진 기록 찾기 및 업데이트
                            List<AiRecommendHistory> historiesToUpdate = aiRecommendHistoryRepository.findByImageUrl(imageUrlInDb);
                            for (AiRecommendHistory history : historiesToUpdate) {
                                history.setImageUrl(null);
                                aiRecommendHistoryRepository.save(history); // DB에 변경 사항 저장
                                dbUpdatedCounter.incrementAndGet(); // AtomicLong 값 증가
                                log.info("Updated AiRecommendHistory id: {} imageUrl to null. File: {}", history.getId(), fileName);
                            }
                        } catch (IOException e) {
                            log.error("파일 삭제 중 오류: {} - {}", fileName, e.getMessage());
                        } catch (Exception e) { // DB 업데이트 중 발생할 수 있는 다른 예외 처리
                            log.error("DB 업데이트 중 오류 (파일: {}): {}", fileName, e.getMessage());
                        }
                    });

        } catch (IOException e) {
            log.error("이미지 정리 중 디렉토리 탐색 오류: {}", e.getMessage());
        }
        log.info("이미지 정리 스케줄러 종료. 삭제된 파일 수: {}", filesDeletedCounter.get(), dbUpdatedCounter.get());
    }

    // "1h" → Duration 변환 함수 (확장 가능)
    private Duration parseDuration(String value) {
        value = value.trim().toLowerCase();
        if (value.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(value.replace("h", "")));
        } else if (value.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(value.replace("m", "")));
        } else if (value.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(value.replace("s", "")));
        }
        throw new IllegalArgumentException("지원 안되는 포맷: " + value);
    }
}