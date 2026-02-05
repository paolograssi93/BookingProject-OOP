package com.bookingproject.booking.exception;

//Gestisce errori di validazione (es. numero documento errato)
public class InvalidInputException extends RuntimeException{
    public InvalidInputException(String message){
        super(message);
    }
}
