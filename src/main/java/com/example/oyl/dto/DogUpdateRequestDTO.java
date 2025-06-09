package com.example.oyl.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class DogUpdateRequestDTO {
    private String name;
    private String breed;
    private LocalDate birthDate;
    private String gender;
    private BigDecimal weight;
    private String notes;
}
