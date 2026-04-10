package com.effectivehygiene.hms.domain.exception;


import com.effectivehygiene.hms.api.error.ApiErrorResponse;
import com.effectivehygiene.hms.api.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1) DTO validation failures (@Valid on request DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {

        String message = buildValidationMessage(ex);

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                400,
                ErrorCode.VALIDATION_FAILED,
                message,
                request.getRequestURI()
        );

        // Log full details server-side
        log.warn("Validation failed: path={}, message={}", request.getRequestURI(), message, ex);

        return ResponseEntity.badRequest().body(body);
    }

    private String buildValidationMessage(MethodArgumentNotValidException ex) {
        // Keep it simple for MVP: first field error message
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            return "Validation failed.";
        }
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    // 2) Domain/business exceptions
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException ex,
                                                         HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                ex.getStatus().value(),
                ex.getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        // Log full details server-side (warn is fine for expected business errors)
        log.warn("Domain error: code={}, status={}, path={}, message={}",
                ex.getCode(), ex.getStatus().value(), request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // 3) Authorization errors that happen in controller layer
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                               HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                403,
                ErrorCode.FORBIDDEN,
                "Access is denied.",
                request.getRequestURI()
        );

        log.warn("Access denied: path={}", request.getRequestURI(), ex);
        return ResponseEntity.status(403).body(body);
    }

    // 4) Catch-all unexpected errors (safe 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex,
                                                             HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                500,
                ErrorCode.INTERNAL_ERROR,
                "Unexpected error occurred.",
                request.getRequestURI()
        );

        // Log full details server-side
        log.error("Unexpected error: path={}", request.getRequestURI(), ex);

        return ResponseEntity.status(500).body(body);
    }
}

