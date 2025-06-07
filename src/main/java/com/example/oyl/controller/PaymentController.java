package com.example.oyl.controller;

import com.example.oyl.dto.PaymentRequestDTO;
import com.example.oyl.dto.PaymentResponseDTO;
import com.example.oyl.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ✅ 결제 생성
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequestDTO dto) {
        try {
            PaymentResponseDTO response = paymentService.createPayment(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ 결제 상태 'PAID'로 전환
    @PatchMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String reservationId) {
        try {
            paymentService.confirmPayment(reservationId);
            return ResponseEntity.ok("결제가 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ 결제 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getPayment(@PathVariable String reservationId) {
        try {
            PaymentResponseDTO response = paymentService.getPaymentByReservationId(reservationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
