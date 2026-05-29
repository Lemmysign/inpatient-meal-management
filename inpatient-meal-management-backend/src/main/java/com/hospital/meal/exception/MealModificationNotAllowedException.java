package com.hospital.meal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MealModificationNotAllowedException extends RuntimeException {

    public MealModificationNotAllowedException(String message) {
        super(message);
    }

    public MealModificationNotAllowedException(String mealType, String deadline) {
        super(String.format("Cannot modify %s order. Modification deadline (%s) has passed.",
                mealType.toLowerCase(), deadline));
    }
}