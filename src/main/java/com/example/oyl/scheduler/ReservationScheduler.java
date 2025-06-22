package com.example.oyl.scheduler;

import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.ReservationStatus;
import com.example.oyl.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedDelay = 86400000) // 24시간 (하루)마다 실행
    public void autoCompleteReservations() {
        LocalDate today = LocalDate.now();
        List<Reservation> targets = reservationRepository
                .findByReservationDateBeforeAndReservationStatus(today, ReservationStatus.RESERVED);

        for (Reservation r : targets) {
            r.setReservationStatus(ReservationStatus.COMPLETED);
        }

        reservationRepository.saveAll(targets);

        if (log.isDebugEnabled()) {
            log.debug("[스케줄러] {}건의 예약을 COMPLETED 상태로 자동 변경 완료!", targets.size());
        }
    }
}
