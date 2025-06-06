package com.example.oyl.controller;

import com.example.oyl.domain.*;
import com.example.oyl.dto.CancelReservationDTO;
import com.example.oyl.dto.ReservationRequestDTO;
import com.example.oyl.dto.ReservationResponseDTO;
import com.example.oyl.dto.ReservationSummaryDTO;
import com.example.oyl.repository.DogRepository;
import com.example.oyl.repository.ReservationRepository;
import com.example.oyl.repository.SpaServiceRepository;
import com.example.oyl.repository.UserRepository;
import com.example.oyl.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final SpaServiceRepository spaServiceRepository;

    private final ReservationService reservationService;

    // ✅ 예약 등록 (서비스에서 유효성 검증 + 저장)
    @PostMapping
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequestDTO dto) {
        try {
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            reservationService.createReservation(dto, email); // 여기서 서비스 호출됨!
            return ResponseEntity.ok("예약 완료! 🐶🛁");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("에러: " + e.getMessage());
        }
    }

    // ✅ 내 예약 리스트 조회 (마이페이지)
    @GetMapping("/mypage/reservations")
    public ResponseEntity<List<ReservationSummaryDTO>> getMyReservations() {
        // JWT에서 로그인한 유저의 이메일 꺼내기
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 해당 유저의 예약 목록 조회
        List<Reservation> reservations = reservationRepository.findByUser(user);

        // 엔티티 → DTO 변환
        List<ReservationSummaryDTO> dtoList = reservations.stream()
                .map(reservation -> ReservationSummaryDTO.builder()
                        .reservationId(reservation.getReservationId())
                        .userNickname(user.getNickname())
                        .dogName(reservation.getDog().getName())
                        .serviceName(reservation.getSpaService().getName())
                        .reservationDate(reservation.getReservationDate())
                        .reservationTime(reservation.getReservationTime())
                        .reservationStatus(reservation.getReservationStatus())
                        .refundStatus(reservation.getRefundStatus())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    // ✅ 예약 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationDetail(@PathVariable("id") String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("해당 예약이 존재하지 않습니다."));

        ReservationResponseDTO dto = ReservationResponseDTO.builder()
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

        return ResponseEntity.ok(dto);

    }

    // ✅ 예약 취소
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelReservation(
            @RequestBody CancelReservationDTO dto,
            Principal principal
    ) {
        String email = principal.getName();
        reservationService.cancelReservation(email, dto);
        return ResponseEntity.ok(Map.of("message", "예약이 성공적으로 취소되었습니다."));
    }

}