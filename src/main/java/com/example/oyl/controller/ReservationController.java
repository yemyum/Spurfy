package com.example.oyl.controller;

import com.example.oyl.common.ApiResponse;
import com.example.oyl.domain.*;
import com.example.oyl.dto.*;
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

    private final ReservationService reservationService;

    // ✅ 예약+결제 동시 등록
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> reserveAndPay(
            @RequestBody ReservationPaymentRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ReservationResponseDTO response = reservationService.reserveAndPay(dto, email);
        return ResponseEntity.ok(
                ApiResponse.<ReservationResponseDTO>builder()
                        .code("S001")
                        .message("예약+결제 완료!")
                        .data(response)
                        .build()
        );
    }

    // ✅ 내 예약 리스트 조회 (마이페이지)
    @GetMapping("/mypage/reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> getMyReservations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ReservationResponseDTO> dtoList = reservationService.getMyReservations(email);
        return ResponseEntity.ok(
                ApiResponse.<List<ReservationResponseDTO>>builder()
                        .code("S001")
                        .message("예약 리스트 조회 성공!")
                        .data(dtoList)
                        .build()
        );
    }

    // ✅ 예약 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> getReservationDetail(@PathVariable("id") String reservationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ReservationResponseDTO dto = reservationService.getReservationDetail(email, reservationId);
        return ResponseEntity.ok(
                ApiResponse.<ReservationResponseDTO>builder()
                        .code("S001")
                        .message("예약 상세 조회 성공!")
                        .data(dto)
                        .build()
        );
    }

    // ✅ 예약 취소
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@RequestBody CancelReservationDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reservationService.cancelReservation(email, dto);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("S001")
                        .message("예약이 성공적으로 취소되었습니다.")
                        .data(null)
                        .build()
        );
    }
}