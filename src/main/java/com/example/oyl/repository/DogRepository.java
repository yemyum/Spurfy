package com.example.oyl.repository;

import com.example.oyl.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DogRepository extends JpaRepository<Dog, String> {

    List<Dog> findByUser_UserId(String userId);

    Optional<Dog> findByDogIdAndUserEmail(String dogId, String userEmail);

}
