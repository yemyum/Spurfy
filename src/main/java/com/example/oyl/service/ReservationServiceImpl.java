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
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final SpaServiceRepository spaServiceRepository;
    private final PaymentRepository paymentRepository;

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

        // enum 빼고 문자열로 세팅!
        reservation.setReservationStatus("CANCELED"); // 취소됨
        reservation.setCancelReason(dto.getCancelReason());
        reservation.setRefundStatus("WAITING"); // 환불 대기
        reservation.setRefundType("자동");
        reservation.setRefundedAt(null);

        reservationRepository.save(reservation);
    }

    // 예약 등록
    @Transactional
    @Override
    public ReservationResponseDTO reserveOnly(String email, ReservationRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Dog dog = dogRepository.findById(dto.getDogId())
                .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));

        SpaService spa = spaServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND));

        // 1. 예약 정보 저장
        Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .user(user)
                .dog(dog)
                .spaService(spa)
                .reservationDate(LocalDate.parse(dto.getReservationDate()))
                .reservationTime(LocalTime.parse(dto.getReservationTime()))
                .reservationStatus("RESERVED") // 예약 완료
                .refundStatus("NONE") // 환불 없음
                .createdAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        // 2. 결제 정보도 payments 테이블에 저장! (이 부분 추가)
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .reservation(reservation) // 외래키 연결
                .user(user)
                .amount(BigDecimal.valueOf(spa.getPrice()))  // BigDecimal 변환
                .paymentMethod("CARD") // 더미
                .paymentStatus("SUCCESS")  // 성공 처리
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment); // payments 저장!

        return ReservationResponseDTO.from(reservation);
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
    private void validateNotPastDate(LocalDate reservationDate, LocalTime reservationTime) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (reservationDate.isBefore(today)) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }

        if (reservationDate.isEqual(today) && reservationTime.isBefore(now)) {
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