package com.example.oyl.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "spa_services")
public class SpaService {

    @Id
    private String serviceId; // ex: spa_001

    private String name;
    private String description;
    private int price;
}
