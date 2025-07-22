package com.example.oyl.scheduler;

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
import java.util.stream.Stream;

@Slf4j
@Component
public class ImageCleanupScheduler {

    // 이미지 저장 기본 디렉토리 이름 (DogImageService와 동일하게 맞춰야 함)
    private final String uploadDir = "uploads";
    private final Path uploadPath; // 실제 파일 경로를 나타내는 Path 객체
    private final Duration imageRetentionDuration;

    // 생성자에서 값 세팅!
    public ImageCleanupScheduler(
            @Value("${app.image.retention-hours:1h}") String retentionDurationRaw // String으로 받아야 함!!
    ) throws IOException {
        // String → Duration으로 직접 변환
        this.imageRetentionDuration = parseDuration(retentionDurationRaw);

        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
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
        long filesDeleted = 0;

        try (Stream<Path> walk = Files.walk(uploadPath)) {
            filesDeleted = walk
                    .filter(Files::isRegularFile)
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
                    .peek(path -> {
                        try {
                            Files.delete(path);
                            log.info("🗑️ 오래된 이미지 파일 삭제됨: {}", path.getFileName());
                        } catch (IOException e) {
                            log.error("파일 삭제 중 오류: {} - {}", path.getFileName(), e.getMessage());
                        }
                    })
                    .count();
        } catch (IOException e) {
            log.error("이미지 정리 중 디렉토리 탐색 오류: {}", e.getMessage());
        }
        log.info("이미지 정리 스케줄러 종료. 삭제된 파일 수: {}", filesDeleted);
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