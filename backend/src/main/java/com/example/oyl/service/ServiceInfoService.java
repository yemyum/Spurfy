package com.example.oyl.service;

import com.example.oyl.dto.ServiceInfoDTO;
import com.example.oyl.repository.ServiceInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceInfoService {

    private final ServiceInfoRepository repository;

    public List<ServiceInfoDTO> getAllActiveInfosSorted() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(info -> new ServiceInfoDTO(info.getTitle(), info.getContent()))
                .collect(Collectors.toList());
    }
}
