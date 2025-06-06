package com.example.oyl.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelReservationDTO {
    private String reservationId;
    private String cancelReason;
}
