package com.effectivehygiene.hms.domain.exception;

import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateEntityException extends DomainException {
    public DuplicateEntityException(String message) {
        super(ErrorCode.DUPLICATE_ENTITY, HttpStatus.CONFLICT, message);
    }
}

