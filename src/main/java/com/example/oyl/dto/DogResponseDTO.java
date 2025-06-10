package com.example.oyl.dto;

import com.example.oyl.domain.Dog;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DogResponseDTO {
    private String dogId;
    private String name;
    private String breed;
    private String birthDate;
    private String gender;
    private BigDecimal weight;
    private String notes;

    public static DogResponseDTO fromEntity(Dog dog) {
        return DogResponseDTO.builder()
                .dogId(dog.getDogId())
                .name(dog.getName())
                .breed(dog.getBreed())
                .birthDate(dog.getBirthDate().toString())
                .gender(dog.getGender())
                .weight(dog.getWeight())
                .notes(dog.getNotes())
                .build();
    }
}