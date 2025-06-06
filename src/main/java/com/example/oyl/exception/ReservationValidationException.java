package com.example.oyl.exception;

public class ReservationValidationException extends RuntimeException{
    public ReservationValidationException(String message) {
        super(message);
    }
}
