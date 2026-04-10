package com.effectivehygiene.hms.api.error;


public enum ErrorCode {
    // Generic
    INTERNAL_ERROR,
    VALIDATION_FAILED,

    // Domain
    ENTITY_NOT_FOUND,
    INACTIVE_ENTITY,
    IMMUTABLE_RECORD,
    INVALID_TRAINING,

    // Security
    UNAUTHENTICATED,
    FORBIDDEN
}
