package com.example.oyl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.yml에서 AI 챗봇 이미지 경로를 가져옴
    @Value("${ai-chatbot.upload-dir}")
    private String aiChatbotUploadDir;

    // application.yml에서 강아지 이미지 경로를 가져옴
    @Value("${dog.upload-dir}")
    private String dogUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // AI 챗봇 이미지 파일 경로 매핑
        // "/api/images/**" URL 요청을 실제 파일 시스템의 "ai_chatbot_images" 폴더로 연결
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:///" + aiChatbotUploadDir);

        // 강아지 이미지 파일 경로 매핑
        // "/dog-images/**" URL 요청을 실제 파일 시스템의 "dog_images" 폴더로 연결
        registry.addResourceHandler("/dog-images/**")
                .addResourceLocations("file:///" + dogUploadDir);
    }
}
