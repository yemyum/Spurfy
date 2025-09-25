package com.example.oyl.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponseDTO {
    private String accessToken;

    public TokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
