package com.example.oyl.scheduler;

import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.ReservationStatus;
import com.example.oyl.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    // 실제 서비스 종료 시점 기준으로 상태를 전환
    @Transactional
    @Scheduled(fixedDelayString = "${app.reservation.auto-complete-interval-ms}")  // n분 간격으로 실행
    public void autoCompleteReservations() {
        LocalDateTime now = LocalDateTime.now();  // 현재 시간 저장

        List<Reservation> targets =
                reservationRepository.findAllByStatus(ReservationStatus.RESERVED);  // 예약된+이용완료전 리스트 가져옴

        int count = 0;

        // 검사 시작!
        for (Reservation r : targets) {  // targets 안의 예약들을 하나씩 꺼냄 (r 처리)

            LocalDateTime startTime =
                    LocalDateTime.of(r.getReservationDate(), r.getReservationTime());  // 예약 날짜+시간을 합침

            LocalDateTime endTime =
                    startTime.plusMinutes(r.getSpaService().getDurationMinutes());  // 서비스 소요 시간만큼 더해 종료 시간 계산

            // "끝나는 시간이 현재시간보다 과거?"인지 체크 (=이미 서비스가 끝?)
            if (endTime.isBefore(now)) {
                r.setReservationStatus(ReservationStatus.COMPLETED);  // 이용완료로 바꿈
                count++;                                              // 완료 처리 수 +1
            }
        }

        log.info("[스케줄러] {}건 자동 완료 처리", count);
    }
}
