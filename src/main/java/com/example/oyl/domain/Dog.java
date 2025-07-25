package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dogs")
public class Dog {

    @Id
    @Column(name = "dog_id")
    private String dogId; // ex: dog_001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    private String breed;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String gender;

    private BigDecimal weight;

    private String notes;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public void updateDog(String name, String breed, LocalDate birthDate, String gender, BigDecimal weight, String notes) {
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.notes = notes;
    }
}
