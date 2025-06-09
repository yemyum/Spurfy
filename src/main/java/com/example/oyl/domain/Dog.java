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

    @ManyToOne
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
}
