package com.effectivehygiene.hms.domain.exception;


import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }
}

