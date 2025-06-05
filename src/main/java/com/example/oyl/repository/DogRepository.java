package com.example.oyl.repository;

import com.example.oyl.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DogRepository extends JpaRepository<Dog, String> {
}
