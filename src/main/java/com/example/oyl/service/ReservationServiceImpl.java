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
    private final ReviewRepository reviewRepository;

    // 예약+결제 동시 (이전과 동일, 여기는 수정 안 함)
    @Override
    public ReservationResponseDTO reserveAndPay(ReservationPaymentRequestDTO dto, String userEmail) {
        try {
            System.out.println("🎯 [START] reserveAndPay 진입");
            System.out.println("📧 userEmail = " + userEmail);
            System.out.println("🐶 dogId = " + dto.getDogId());
            System.out.println("🛁 serviceId = " + dto.getServiceId());
            System.out.println("💸 amount = " + dto.getAmount());

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            System.out.println("✅ user 찾음");

            Dog dog = dogRepository.findById(dto.getDogId())
                    .orElseThrow(() -> new CustomException(ErrorCode.DOG_NOT_FOUND));
            System.out.println("✅ dog 찾음");

            SpaService service = spaServiceRepository.findById(dto.getServiceId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SPA_SERVICE_NOT_FOUND));
            System.out.println("✅ service 찾음");

            Long servicePrice = service.getPrice(); // SpaService에서 가격을 가져옴
            if (servicePrice == null || servicePrice <= 0) {
                throw new CustomException(ErrorCode.INVALID_INPUT); // 가격이 유효하지 않으면 예외 처리
            }
            // 요청 DTO의 amount와 실제 서비스 가격이 일치하는지 확인하는 로직 추가 가능
            if (BigDecimal.valueOf(servicePrice).compareTo(BigDecimal.valueOf(dto.getAmount())) != 0) {
                throw new CustomException(ErrorCode.INVALID_INPUT); // 금액 불일치 예외 처리 (선택 사항)
            }

            LocalDate date = LocalDate.parse(dto.getReservationDate());
            System.out.println("📅 예약 날짜: " + date);

            validateReservationOwnership(dog, user);
            validateNotPastDate(date);
            validateDuplicateReservation(dog, date);
            System.out.println("✅ 유효성 검증 통과");

            Reservation reservation = Reservation.builder()
                    .reservationId(UUID.randomUUID().toString())
                    .user(user)
                    .dog(dog)
                    .spaService(service)
                    .reservationDate(date)
                    .reservationTime(LocalTime.parse(dto.getReservationTime()))
                    .price(servicePrice)
                    .reservationStatus(ReservationStatus.RESERVED)
                    .refundStatus(RefundStatus.NONE)
                    .refundType(RefundType.FULL)
                    .cancelReason("")
                    .refundedAt(null)
                    .createdAt(LocalDateTime.now())
                    .build();
            reservationRepository.save(reservation);   // INSERT
            System.out.println("✅ 예약 저장 완료");

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
            System.out.println("✅ 결제 저장 완료");

            return ReservationResponseDTO.from(reservation, false);
        } catch (Exception e) {
            System.out.println("💥 예외 발생! " + e.getMessage());
            e.printStackTrace(); // ❗ 콘솔에 에러 로그 출력!!
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // 예약 취소
    @Override
    public void cancelReservation(String userEmail, CancelReservationDTO dto) {
        try { // ⭐ try 블록 시작 ⭐
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

            if (!reservation.getUser().getUserId().equals(user.getUserId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
            }

            if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
                throw new CustomException(ErrorCode.CANNOT_CANCEL_COMPLETED_RESERVATION);
            }

            if (reservation.getReservationDate().isBefore(LocalDate.now())) {
                throw new CustomException(ErrorCode.CANNOT_CANCEL_PAST_RESERVATION);
            }

            // Payment 정보 가져오기 (만약 환불에 Payment 정보가 필요하다면)
            // findByReservation_ReservationId 메서드가 PaymentRepository에 정의되어 있어야 함!
            Payment payment = paymentRepository.findByReservation_ReservationId(reservation.getReservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

            // ⭐ 더미 결제 처리 로직 (실제 PG사 연동 대신 바로 성공 처리) ⭐
            System.out.println("✅ [LOG] 더미 결제이므로 PG사 환불 요청을 성공으로 간주하고 처리합니다.");

            // PaymentStatus 업데이트 (선택 사항) - PaymentStatus enum에 CANCELED가 없다면 PAID 유지
            // payment.setPaymentStatus(PaymentStatus.CANCELED); // PaymentStatus에 CANCELED 추가 후 사용 권장
            // paymentRepository.save(payment);

            reservation.setReservationStatus(ReservationStatus.CANCELED);
            reservation.setCancelReason(dto.getCancelReason());
            reservation.setRefundStatus(RefundStatus.COMPLETED); // 바로 '환불 완료'로!
            reservation.setRefundType(RefundType.AUTO);
            reservation.setRefundedAt(LocalDateTime.now()); // ⭐⭐ 환불 완료 시간 기록! ⭐⭐

            reservationRepository.save(reservation); // 변경된 예약 정보 저장

            System.out.println("✅ 예약 및 환불 상태 업데이트 완료 (더미 환불)");

        } catch (CustomException e) {
            System.err.println("❌ CustomException 발생: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            System.out.println("❌ 예약 취소 처리 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "예약 취소 중 시스템 오류가 발생했습니다.");
        }
    }

    // 내 예약 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Reservation> reservations = reservationRepository.findByUser_UserIdWithDetails(user.getUserId());

        return reservations.stream()
                .map(reservation -> {
                    // 해당 예약(reservation)에 대한 리뷰가 ReviewRepository에 존재하는지 확인
                    // Review 엔티티에 reservationId 필드가 String으로 직접 있다면
                    boolean hasReview = reviewRepository.existsByReservation_ReservationId(reservation.getReservationId());

                    // ReservationResponseDTO.from 메서드를 사용하여 DTO로 변환 시, 계산된 hasReview 값도 함께 넘겨줌
                    return ReservationResponseDTO.from(reservation, hasReview);
                })
                .toList();
    }

    // 예약 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationDetail(String userEmail, String reservationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Reservation reservation = reservationRepository.findByIdWithPayment(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_RESERVATION);
        }

        return ReservationResponseDTO.from(reservation, false); // 새로 생성된 리뷰가 없다면 false!
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
        // 이 메서드에게 "이 강아지가 이 날짜에 '취소된' 상태가 아닌 다른 예약이 있는지 확인해줘"
        boolean exists = reservationRepository.existsByDogAndReservationDateAndReservationStatusNot(
                dog,
                reservationDate,
                ReservationStatus.CANCELED // ReservationStatus.CANCELED 상태는 중복으로 보지 않겠다!
        );
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}