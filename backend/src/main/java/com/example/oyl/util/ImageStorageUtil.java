package com.example.oyl.util;

import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ImageStorageUtil {
    private static final String IMAGE_FILE_NAME_FORMAT = "yyyyMMdd_HHmmssSSS";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final String DIRECTORY = "ai_chatbot_images";

    public String save(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_LIMIT_EXCEEDED, "파일 크기는 50MB를 초과할 수 없습니다.");
        }

        try {
            Path uploadPath = Paths.get(DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String extension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFileName.substring(dotIndex);
                originalFileName = originalFileName.substring(0, dotIndex);
            }

            String savedFileName = originalFileName + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(IMAGE_FILE_NAME_FORMAT)) + extension;

            Path filePath = uploadPath.resolve(savedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return savedFileName;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
