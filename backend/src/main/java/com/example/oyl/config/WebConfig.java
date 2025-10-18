package com.example.oyl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.yml에서 AI 챗봇 이미지 경로를 가져옴
    @Value("${ai-chatbot.upload-dir}")
    private String aiChatbotUploadDir;

    // application.yml에서 강아지 이미지 경로를 가져옴
    @Value("${dog.upload-dir}")
    private String dogUploadDir;

    // ⭐️ 경로를 안정적으로 만들어주는 유틸리티 함수
    private String getFileProtocolPath(String uploadDir) {
        // 1. 역슬래시를 슬래시로 통일 (Windows <-> Linux 호환)
        String path = uploadDir.replace(File.separator, "/");
        // 2. 경로가 슬래시로 끝나지 않으면 추가
        if (!path.endsWith("/")) {
            path += "/";
        }
        // 3. 가장 안정적인 'file:' 프로토콜로 반환
        return "file:" + path;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // AI 챗봇 이미지 파일 경로 매핑
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations(getFileProtocolPath(aiChatbotUploadDir));

        // 강아지 이미지 파일 경로 매핑
        registry.addResourceHandler("/dog-images/**")
                .addResourceLocations(getFileProtocolPath(dogUploadDir));
    }
}
