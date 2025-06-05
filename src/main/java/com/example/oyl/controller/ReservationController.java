package com.example.oyl.controller;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.SpaService;
import com.example.oyl.domain.User;
import com.example.oyl.dto.ReservationRequestDTO;
import com.example.oyl.dto.ReservationSummaryDTO;
import com.example.oyl.repository.DogRepository;
import com.example.oyl.repository.ReservationRepository;
import com.example.oyl.repository.SpaServiceRepository;
import com.example.oyl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final SpaServiceRepository spaServiceRepository;

    @PostMapping
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequestDTO dto) {
        try {
            // 🔐 JWT에서 로그인된 사용자 email 추출
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("사용자 없음"));

            // 🐶 강아지 + 서비스 조회
            Dog dog = dogRepository.findById(dto.getDogId()).orElseThrow(() -> new RuntimeException("강아지 없음"));
            SpaService service = spaServiceRepository.findById(dto.getServiceId()).orElseThrow(() -> new RuntimeException("스파 서비스 없음"));

            // 📝 예약 엔티티 생성
            Reservation reservation = Reservation.builder()
                    .reservationId(UUID.randomUUID().toString())
                    .user(user)
                    .dog(dog)
                    .spaService(service)
                    .reservationDate(dto.getReservationDate())
                    .reservationTime(dto.getReservationTime())
                    .reservationStatus(1)        // 기본: 예약 완료
                    .refundStatus(0)             // 기본: 환불 없음
                    .refundType("자동")           // 기본 자동 처리
                    .cancelReason("")
                    .refundedAt(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            System.out.println("🧪 예약 객체: " + reservation);

            // ✅ 저장
            reservationRepository.save(reservation);

            return ResponseEntity.ok("예약 완료! 🐶🛁");
        } catch (Exception e) {
            e.printStackTrace(); // ❗여기 콘솔에 진짜 에러 터진 위치 출력됨
            return ResponseEntity.status(500).body("에러: " + e.getMessage());
        }

    }

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
}