package com.example.oyl.scheduler;

import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.ReservationStatus;
import com.example.oyl.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    // 주기(밀리초) 기본값: 하루(24시간) = 86400000
    @Value("${app.reservation.auto-complete-interval-ms:86400000}")
    private long autoCompleteIntervalMs;

    // 예약 완료로 바꿀 기준일 (오늘 이전만 완료로)
    // 필요하다면 @Value로 기준일 변경 가능
    // @Value("${app.reservation.complete-before-days:0}") // 오늘 이전만 완료로 (기본 0)
    // private int completeBeforeDays;

    // 주기적으로 실행: @Scheduled(fixedDelay = ...) 대신, fixedDelayString!
    @Scheduled(fixedDelayString = "${app.reservation.auto-complete-interval-ms:86400000}")
    public void autoCompleteReservations() {
        LocalDate today = LocalDate.now();
        List<Reservation> targets = reservationRepository
                .findByReservationDateBeforeAndReservationStatus(today, ReservationStatus.RESERVED);

        if (targets.isEmpty()) {
            log.info("[스케줄러] 자동 완료 예약 없음! (오늘: {})", today);
            return;
        }

        for (Reservation r : targets) {
            r.setReservationStatus(ReservationStatus.COMPLETED);
        }
        reservationRepository.saveAll(targets);
        log.info("[스케줄러] {}건의 예약을 COMPLETED 상태로 자동 변경 완료!", targets.size());
    }
}
