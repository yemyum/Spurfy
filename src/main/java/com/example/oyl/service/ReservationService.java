package com.example.oyl.service;

import com.example.oyl.dto.*;

import java.util.List;

public interface ReservationService {

    // 결제 + 예약 등록
    ReservationResponseDTO reserveAndPay(ReservationPaymentRequestDTO dto, String userEmail);

    // 예약 취소
    void cancelReservation(String userEmail, CancelReservationDTO dto);


    // 예약 목록
    List<ReservationSummaryDTO> getMyReservations(String userEmail);

    // 예약 상세조회
    ReservationResponseDTO getReservationDetail(String userEmail, String reservationId);

}
