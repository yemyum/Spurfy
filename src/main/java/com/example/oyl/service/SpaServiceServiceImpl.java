package com.example.oyl.service;

import com.example.oyl.domain.SpaService;
import com.example.oyl.dto.SpaServiceDTO;
import com.example.oyl.dto.SpaServiceSummaryDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.SpaServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaServiceServiceImpl implements SpaServiceService {

    private final SpaServiceRepository spaServiceRepository;

    @Override
    public List<SpaServiceSummaryDTO> getActiveSpaServices() {
        return spaServiceRepository.findByIsActiveTrue().stream()
                .map(this::convertToSpaServiceSummaryDTO)
                .toList();
    }

    @Override
    public SpaServiceDTO getSpaServiceDetail(String serviceId) {
        SpaService spa = spaServiceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND));

        return convertToSpaServiceDTO(spa);
    }

    @Override
    public SpaServiceDTO getSpaServiceBySlug(String spaSlug) {
        log.info("슬러그로 스파 서비스 조회 요청: {}", spaSlug);
        SpaService spaEntity = spaServiceRepository.findBySlug(spaSlug) // Repository에서 findBySlug 호출
                .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND, "해당 슬러그의 스파 서비스를 찾을 수 없습니다: " + spaSlug));
        log.info("슬러그 '{}'에 해당하는 스파 엔티티 발견: {}", spaSlug, spaEntity.getName());
        return convertToSpaServiceDTO(spaEntity); // 엔티티를 DTO로 변환하여 반환
    }

    // SpaService 엔티티를 SpaServiceDTO로 변환하는 헬퍼 메서드
    private SpaServiceDTO convertToSpaServiceDTO(SpaService entity) {
        return SpaServiceDTO.builder()
                .serviceId(entity.getServiceId())
                .name(entity.getName())
                .description(entity.getDescription())
                .durationMinutes(entity.getDurationMinutes())
                .price(entity.getPrice())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .slug(entity.getSlug()) // slug 필드도 DTO에 매핑
                .availableTimes(
                        entity.getAvailableTimes() == null || entity.getAvailableTimes().isBlank()
                                ? List.of()
                                : List.of(entity.getAvailableTimes().split(","))
                )
                .tagNames(
                        entity.getTags() == null
                                ? List.of()
                                : entity.getTags().stream()
                                .map(tag -> tag.getTagName())
                                .toList()
                )
                .build();
    }

    // SpaService 엔티티를 SpaServiceSummaryDTO로 변환하는 헬퍼 메서드
    private SpaServiceSummaryDTO convertToSpaServiceSummaryDTO(SpaService entity) {
        return SpaServiceSummaryDTO.builder()
                .serviceId(entity.getServiceId())
                .name(entity.getName())
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .slug(entity.getSlug())
                .tagNames(
                        entity.getTags() == null
                                ? List.of()
                                : entity.getTags().stream()
                                .map(tag -> tag.getTagName())
                                .toList()
                )
                .build();
    }

}
