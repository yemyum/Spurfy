package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dogs")
public class Dog {

    @Id
    private String dogId; // ex: dog_001

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
