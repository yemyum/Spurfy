package com.example.oyl.dto;

import lombok.Data;

import java.util.List;

@Data
public class GptRequestDTO {
    private String model = "gpt-4o";
    private List<Message> messages;

    @Data
    public static class Message {
        private String role;     // "user" 또는 "system"
        private String content;  // 유저가 보내는 메세지
    }
}
