package com.hospital.meal.exception;

public class OrderingNotAllowedException extends RuntimeException {

    public OrderingNotAllowedException(String message) {
        super(message);
    }

    public OrderingNotAllowedException(String message, String nextWindow) {
        super(message + " " + nextWindow);
    }
}