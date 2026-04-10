package com.effectivehygiene.hms.domain.exception;


import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InactiveEntityException extends DomainException {
    public InactiveEntityException(String message) {
        super(ErrorCode.INACTIVE_ENTITY, HttpStatus.CONFLICT, message);
    }
}
