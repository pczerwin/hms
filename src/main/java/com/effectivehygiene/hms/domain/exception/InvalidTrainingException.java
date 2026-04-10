package com.effectivehygiene.hms.domain.exception;


import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidTrainingException extends DomainException {
    public InvalidTrainingException(String message) {
        super(ErrorCode.INVALID_TRAINING, HttpStatus.BAD_REQUEST, message);
    }
}

