package com.effectivehygiene.hms.domain.exception;

import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class MissingCurrentVersionException extends DomainException {
    public MissingCurrentVersionException(String message) {
        super(ErrorCode.MISSING_CURRENT_VERSION, HttpStatus.CONFLICT, message);
    }
}

