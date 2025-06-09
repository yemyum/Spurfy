package com.example.oyl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DogResponseDTO {
    private String dogId;
    private String name;
    private String breed;
    private LocalDate birthDate;
    private String gender;
    private BigDecimal weight;
}