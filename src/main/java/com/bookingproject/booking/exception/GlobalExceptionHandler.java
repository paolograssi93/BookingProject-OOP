package com.bookingproject.booking.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> hanleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body("Attenzione. Errore: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> hanleGeneralError(Exception e) {
        return ResponseEntity.internalServerError().body("Si è verificato un errore interno: " + e.getMessage());
    }
}
