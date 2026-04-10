package com.effectivehygiene.hms.domain.exception;


import com.effectivehygiene.hms.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

    private final ErrorCode code;
    private final HttpStatus status;

    protected DomainException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public ErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

