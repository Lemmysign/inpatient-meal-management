package com.hospital.meal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InviteExpiredException extends RuntimeException {

    public InviteExpiredException(String message) {
        super(message);
    }

    public InviteExpiredException() {
        super("Invitation has expired. Please request a new invitation.");
    }
}