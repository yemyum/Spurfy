package com.example.oyl.service;

import com.example.oyl.dto.CancelReservationDTO;
import com.example.oyl.dto.ReservationRequestDTO;

public interface ReservationService {

    void cancelReservation(String userEmail, CancelReservationDTO dto);

    // 예약 등록
    void createReservation(ReservationRequestDTO dto, String userEmail);
}
