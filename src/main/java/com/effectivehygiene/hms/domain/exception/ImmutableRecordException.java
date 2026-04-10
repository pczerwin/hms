package com.effectivehygiene.hms.domain.exception;


import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ImmutableRecordException extends DomainException {
    public ImmutableRecordException(String message) {
        super(ErrorCode.IMMUTABLE_RECORD, HttpStatus.CONFLICT, message);
    }
}

