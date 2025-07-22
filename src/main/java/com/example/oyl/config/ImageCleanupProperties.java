package com.example.oyl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.image")
public class ImageCleanupProperties {
    private Duration retentionHours = Duration.ofHours(1); // 자동으로 1h → PT1H 변환됨!
    private long cleanupIntervalMs = 3600000;
}