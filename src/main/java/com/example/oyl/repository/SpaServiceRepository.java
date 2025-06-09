package com.example.oyl.repository;

import com.example.oyl.domain.SpaService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaServiceRepository extends JpaRepository<SpaService, String> {

    List<SpaService> findByIsActiveTrue();

    boolean existsById(String serviceId);// is_active = true

}
