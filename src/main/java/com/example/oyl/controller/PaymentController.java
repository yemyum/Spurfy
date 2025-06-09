package com.example.oyl.controller;

import com.example.oyl.dto.PaymentRequestDTO;
import com.example.oyl.dto.PaymentResponseDTO;
import com.example.oyl.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ✅ 결제 생성 (로그인 사용자 기준)
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequestDTO dto) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            PaymentResponseDTO response = paymentService.createPayment(userEmail, dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ 결제 상태 'PAID'로 전환 (로그인 사용자 기준)
    @PatchMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String reservationId) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            paymentService.confirmPayment(userEmail, reservationId);
            return ResponseEntity.ok("결제가 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ 결제 조회 (로그인 사용자 기준)
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getPayment(@PathVariable String reservationId) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            PaymentResponseDTO response = paymentService.getPaymentByReservationId(userEmail, reservationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
