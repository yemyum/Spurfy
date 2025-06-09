package com.example.oyl.repository;

import com.example.oyl.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DogRepository extends JpaRepository<Dog, String> {

}
