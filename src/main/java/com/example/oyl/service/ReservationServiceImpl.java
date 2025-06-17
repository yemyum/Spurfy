package com.example.oyl.service;

import com.example.oyl.domain.*;
import com.example.oyl.dto.CancelReservationDTO;
import com.example.oyl.dto.ReservationRequestDTO;
import com.example.oyl.dto.ReservationResponseDTO;
import com.example.oyl.dto.ReservationSummaryDTO;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.DogRepository;
import com.example.oyl.repository.ReservationRepository;
import com.example.oyl.repository.SpaServiceRepository;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final SpaServiceRepository spaServiceRepository;

    @Transactional
    @Override
    public void cancelReservation(String userEmail, CancelReservationDTO dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
        }

        reservation.setReservationStatus(ReservationStatus.CANCELED); // 취소됨
        reservation.setCancelReason(dto.getCancelReason());
        reservation.setRefundStatus(RefundStatus.WAITING); // 환불 대기
        reservation.setRefundType("자동");
        reservation.setRefundedAt(null);

        reservationRepository.save(reservation);
    }

    // 예약 등록
    @Transactional
    public void createReservation(ReservationRequestDTO dto, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateReservationOwnership(dto.getDogId(), user);
        validateNotPastDate(dto.getReservationDate());

        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

        validateDuplicateReservation(dog, dto.getReservationDate());
        validateSpaServiceExistence(dto.getServiceId());

        // 저장 로직
        Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .user(user)
                .dog(dog)
                .spaService(spaServiceRepository.findById(dto.getServiceId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND)))
                .reservationDate(dto.getReservationDate())
                .reservationTime(dto.getReservationTime())
                .reservationStatus(ReservationStatus.RESERVED)
                .refundStatus(RefundStatus.NONE)
                .refundType("자동")
                .cancelReason("")
                .refundedAt(null)
                .createdAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);
    }

    // 1단계 : 본인 강아지인지 확인
    private void validateReservationOwnership(String dogId, User user) {
        Dog dog = dogRepository.findById(dogId)
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

        if (!dog.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_DOG);
        }
    }

    // 2단계 : 과거 날짜 방지
    private void validateNotPastDate(LocalDate reservationDate) {
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }
    }

    // 3단계 : 중복 예약 방지
    private void validateDuplicateReservation(Dog dog, LocalDate reservationDate) {
        boolean exists = reservationRepository.existsByDogAndReservationDate(dog, reservationDate);

        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    // 4단계 : 스파 서비스 ID 유효성 확인
    private void validateSpaServiceExistence(String serviceId) {
        boolean exists = spaServiceRepository.existsById(serviceId);

        if (!exists) {
            throw new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND);
        }
    }

    @Override
    public List<ReservationSummaryDTO> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Reservation> reservations = reservationRepository.findByUser(user);

        return reservations.stream()
                .map(reservation -> new ReservationSummaryDTO(
                        reservation.getReservationId(),
                        user.getNickname(),                             // 닉네임
                        reservation.getDog().getName(),                 // 강아지 이름
                        reservation.getSpaService().getName(),          // 서비스 이름
                        reservation.getReservationDate(),               // 예약 날짜
                        reservation.getReservationTime(),               // 예약 시간
                        reservation.getReservationStatus(),             // 예약 상태
                        reservation.getRefundStatus()                   // 환불 상태
                ))
                .toList();
    }

    @Override
    public ReservationResponseDTO getReservationDetail(String userEmail, String reservationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
        }

        return ReservationResponseDTO.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUser().getUserId())
                .dogId(reservation.getDog().getDogId())
                .serviceId(reservation.getSpaService().getServiceId())
                .dogName(reservation.getDog().getName())
                .serviceName(reservation.getSpaService().getName())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .reservationStatus(reservation.getReservationStatus())
                .refundStatus(reservation.getRefundStatus())
                .refundType(reservation.getRefundType())
                .cancelReason(reservation.getCancelReason())
                .refundedAt(reservation.getRefundedAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

}