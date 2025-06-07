package com.example.oyl.service;

import com.example.oyl.domain.Payment;
import com.example.oyl.domain.PaymentStatus;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.User;
import com.example.oyl.dto.PaymentRequestDTO;
import com.example.oyl.dto.PaymentResponseDTO;
import com.example.oyl.repository.PaymentRepository;
import com.example.oyl.repository.ReservationRepository;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {

        // 중복 결제 방지
        if (paymentRepository.existsByReservation_ReservationId(dto.getReservationId())) {
            throw new RuntimeException("이미 해당 예약에 대한 결제가 존재합니다.");
        }

        // 예약 ID로 예약 존재 확인
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 사용자 ID로 유저 확인
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 결제 엔티티 생성
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .reservation(reservation)
                .user(user)
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())// 기본은 PENDING으로
                .build();

        // 저장
        paymentRepository.save(payment);

        return PaymentResponseDTO.from(payment);
    }

    @Override
    public PaymentResponseDTO getPaymentByReservationId(String reservationId) {
        Payment payment = paymentRepository.findFirstByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("해당 예약의 결제 정보가 없습니다."));

        return PaymentResponseDTO.from(payment);
    }

    @Override
    @Transactional
    public void confirmPayment(String reservationId) {
        Payment payment = paymentRepository.findFirstByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("해당 예약에 대한 결제 정보가 없습니다."));

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("이미 결제 완료된 예약입니다.");
        }

        payment.setPaymentStatus(PaymentStatus.PAID);

        Reservation reservation = payment.getReservation();
        reservation.setPaymentStatus("PAID");
    }
}
