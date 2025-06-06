package com.example.oyl.controller;

import com.example.oyl.dto.SpaServiceDTO;
import com.example.oyl.dto.SpaServiceSummaryDTO;
import com.example.oyl.service.SpaServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spa-services")
public class SpaServiceController {

    private final SpaServiceService spaServiceService;

    @GetMapping
    public ResponseEntity<List<SpaServiceSummaryDTO>> getActiveSpaServices() {
        List<SpaServiceSummaryDTO> services = spaServiceService.getActiveSpaServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaServiceDTO> getSpaServiceDetail(@PathVariable("id") String serviceId) {
        SpaServiceDTO dto = spaServiceService.getSpaServiceDetail(serviceId);

        return ResponseEntity.ok(dto);
    }
}
