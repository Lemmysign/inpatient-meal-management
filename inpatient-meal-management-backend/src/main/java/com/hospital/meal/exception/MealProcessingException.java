package com.hospital.meal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MealProcessingException extends RuntimeException {

    public MealProcessingException(String message) {
        super(message);
    }
}