package com.example.oyl.service;

import com.example.oyl.domain.*;
import com.example.oyl.dto.*;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final SpaServiceRepository spaServiceRepository;
    private final PaymentRepository paymentRepository;

    // 예약+결제 동시
    @Override
    public ReservationResponseDTO reserveAndPay(ReservationPaymentRequestDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));
        SpaService service = spaServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND));

        LocalDate date = LocalDate.parse(dto.getReservationDate());
        validateReservationOwnership(dog, user);
        validateNotPastDate(date);
        validateDuplicateReservation(dog, date);

        Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .user(user)
                .dog(dog)
                .spaService(service)
                .reservationDate(date)
                .reservationTime(LocalTime.parse(dto.getReservationTime()))
                .reservationStatus(ReservationStatus.RESERVED)
                .refundStatus(RefundStatus.NONE)
                .refundType(RefundType.FULL)
                .cancelReason("")
                .refundedAt(null)
                .createdAt(LocalDateTime.now())
                .build();
        reservationRepository.save(reservation);   // INSERT

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .reservation(reservation)
                .user(user)
                .amount(BigDecimal.valueOf(dto.getAmount()))
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        return ReservationResponseDTO.from(reservation);
    }

    // 예약 취소
    @Override
    public void cancelReservation(String userEmail, CancelReservationDTO dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
        }

        reservation.setReservationStatus(ReservationStatus.CANCELED);
        reservation.setCancelReason(dto.getCancelReason());
        reservation.setRefundStatus(RefundStatus.WAITING);
        reservation.setRefundType(RefundType.AUTO);
        reservation.setRefundedAt(null);

        reservationRepository.save(reservation);
    }

    // 내 예약 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReservationSummaryDTO> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Reservation> reservations = reservationRepository.findByUser(user);

        return reservations.stream()
                .map(reservation -> new ReservationSummaryDTO(
                        reservation.getReservationId(),
                        user.getNickname(),
                        reservation.getDog().getName(),
                        reservation.getSpaService().getName(),
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getReservationStatus(),
                        reservation.getRefundStatus()
                ))
                .toList();
    }

    // 예약 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationDetail(String userEmail, String reservationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
        }

        return ReservationResponseDTO.from(reservation);
    }

    // ======= private 검증 함수들 반드시 필요! =======
    private void validateReservationOwnership(Dog dog, User user) {
        if (!dog.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_DOG);
        }
    }

    private void validateNotPastDate(LocalDate reservationDate) {
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }
    }

    private void validateDuplicateReservation(Dog dog, LocalDate reservationDate) {
        boolean exists = reservationRepository.existsByDogAndReservationDate(dog, reservationDate);
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}