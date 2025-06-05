package com.example.oyl.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReservationRequestDTO {
    private String userId;            // (optional) → JWT로 대체 가능
    private String dogId;             // 필수: 어떤 강아지가 예약하는지
    private String serviceId;         // 필수: 어떤 스파 서비스인지

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate; // 필수: 예약일

    @JsonFormat(pattern = "HH:mm")
    private LocalTime reservationTime; // 필수: 예약 시간
}
