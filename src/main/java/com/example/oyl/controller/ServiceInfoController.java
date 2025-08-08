package com.example.oyl.controller;

import com.example.oyl.dto.ServiceInfoDTO;
import com.example.oyl.service.ServiceInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-info")
public class ServiceInfoController {

    private final ServiceInfoService service;

    @GetMapping
    public ResponseEntity<List<ServiceInfoDTO>> getAllActiveInfosSorted() {
        return ResponseEntity.ok(
                service.getAllActiveInfosSorted()
        );
    }
}
