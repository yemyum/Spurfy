package com.example.oyl.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class DogRequestDTO {
    private String name;
    private String breed;
    private LocalDate birthDate;
    private String gender;               // "M", "F"
    private BigDecimal weight;
    private String notes;
}
