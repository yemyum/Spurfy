package com.example.oyl.service;

import com.example.oyl.dto.CancelReservationDTO;
import com.example.oyl.dto.ReservationRequestDTO;
import com.example.oyl.dto.ReservationResponseDTO;
import com.example.oyl.dto.ReservationSummaryDTO;

import java.util.List;

public interface ReservationService {

    // 예약 등록
    void createReservation(ReservationRequestDTO dto, String userEmail);

    // 예약 취소
    void cancelReservation(String userEmail, CancelReservationDTO dto);


    // 예약 목록
    List<ReservationSummaryDTO> getMyReservations(String userEmail);

    // 예약 상세조회
    ReservationResponseDTO getReservationDetail(String userEmail, String reservationId);

}
