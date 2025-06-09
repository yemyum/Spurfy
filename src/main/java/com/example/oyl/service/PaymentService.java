package com.example.oyl.service;

import com.example.oyl.domain.Payment;
import com.example.oyl.dto.PaymentRequestDTO;
import com.example.oyl.dto.PaymentResponseDTO;

import java.util.Optional;

public interface PaymentService {

    // 결제 생성
    PaymentResponseDTO createPayment(String userEmail, PaymentRequestDTO dto);

    // 결제 조회 (예약 Id 기준)
    PaymentResponseDTO getPaymentByReservationId(String userEmail, String reservationId);

    void confirmPayment(String userEmail, String reservationId);

}
