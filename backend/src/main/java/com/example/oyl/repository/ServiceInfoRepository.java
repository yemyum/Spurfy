package com.example.oyl.repository;

import com.example.oyl.domain.ServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceInfoRepository extends JpaRepository<ServiceInfo, Long> {

    List<ServiceInfo> findByIsActiveTrueOrderByDisplayOrderAsc();

}
