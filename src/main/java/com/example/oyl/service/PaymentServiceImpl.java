package com.example.oyl.service;

import com.example.oyl.domain.Payment;
import com.example.oyl.domain.PaymentStatus;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.User;
import com.example.oyl.dto.PaymentRequestDTO;
import com.example.oyl.dto.PaymentResponseDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
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
    public PaymentResponseDTO createPayment(String userEmail, PaymentRequestDTO dto) {

        // 중복 결제 방지
        if (paymentRepository.existsByReservation_ReservationId(dto.getReservationId())) {
            throw new CustomException(ErrorCode.DUPLICATE_PAYMENT);
        }

        // 예약 ID로 예약 존재 확인
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // JWT 토큰에서 가져온 이메일로 유저 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 예약의 주인과 일치하는지 검증
        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.PAYMENT_UNAUTHORIZED);

        }
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

        // 저장 꼭!
        paymentRepository.save(payment);

        return PaymentResponseDTO.from(payment);
    }

    @Override
    public PaymentResponseDTO getPaymentByReservationId(String userEmail, String reservationId) {
        Payment payment = paymentRepository.findFirstByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

            // 결제 당사자(유저)가 본인인지 체크!
            if (!payment.getUser().getEmail().equals(userEmail)) {
                throw new CustomException(ErrorCode.PAYMENT_UNAUTHORIZED);
            }

        return PaymentResponseDTO.from(payment);
    }

    @Override
    @Transactional
    public void confirmPayment(String userEmail, String reservationId) {
        Payment payment = paymentRepository.findFirstByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

            // 결제 당사자(유저)가 본인인지 체크!
            if (!payment.getUser().getEmail().equals(userEmail)) {
                throw new CustomException(ErrorCode.PAYMENT_UNAUTHORIZED);
        }


            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        payment.setPaymentStatus(PaymentStatus.PAID);

        Reservation reservation = payment.getReservation();
        reservation.setPaymentStatus("PAID");
    }
}
