package com.example.oyl.service;

import com.example.oyl.domain.SpaService;
import com.example.oyl.dto.SpaServiceDTO;
import com.example.oyl.dto.SpaServiceSummaryDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.SpaServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaServiceServiceImpl implements SpaServiceService {

    private final SpaServiceRepository spaServiceRepository;

    @Override
    public List<SpaServiceSummaryDTO> getActiveSpaServices() {
        return spaServiceRepository.findByIsActiveTrue().stream()
                .map(spa -> SpaServiceSummaryDTO.builder()
                        .serviceId(spa.getServiceId())
                        .name(spa.getName())
                        .price(spa.getPrice())
                        .durationMinutes(spa.getDurationMinutes())
                        .build())
                .toList();
    }

    @Override
    public SpaServiceDTO getSpaServiceDetail(String serviceId) {
        SpaService spa = spaServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND));

        return SpaServiceDTO.builder()
                .serviceId(spa.getServiceId())
                .name(spa.getName())
                .description(spa.getDescription())
                .durationMinutes(spa.getDurationMinutes())
                .price(spa.getPrice())
                .isActive(spa.isActive())
                .createdAt(spa.getCreatedAt())
                .updatedAt(spa.getCreatedAt())
                .build();
    }
}
