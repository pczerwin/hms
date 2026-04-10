package com.effectivehygiene.hms.api.error;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,   // UTC
        int status,          // HTTP status code
        ErrorCode code,      // stable machine-readable code
        String message,      // human-readable message
        String path          // request path
) {}
