package com.example.oyl.service;

import com.example.oyl.dto.SpaServiceDTO;
import com.example.oyl.dto.SpaServiceSummaryDTO;

import java.util.List;

public interface SpaServiceService {

    List<SpaServiceSummaryDTO> getActiveSpaServices();

    SpaServiceDTO getSpaServiceDetail(String serviceId);
}
