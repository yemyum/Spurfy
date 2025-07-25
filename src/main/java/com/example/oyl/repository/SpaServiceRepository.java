package com.example.oyl.repository;

import com.example.oyl.domain.SpaService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaServiceRepository extends JpaRepository<SpaService, String> {

    List<SpaService> findByIsActiveTrue();

    boolean existsById(String serviceId);// is_active = true

    Optional<SpaService> findByName(String name);

    Optional<SpaService> findBySlug(String slug);

}
