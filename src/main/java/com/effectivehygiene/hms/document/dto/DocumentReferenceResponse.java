package com.effectivehygiene.hms.document.dto;

import java.time.Instant;

public record DocumentReferenceResponse(
        Long id,
        String referenceCode,
        String originDepartment,
        boolean mandatory,
        boolean active,
        Instant createdAt
) {
}

