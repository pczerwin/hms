package com.effectivehygiene.hms.document.dto;

import java.time.Instant;
import java.time.LocalDate;

public record DocumentVersionResponse(
        Long id,
        Long documentReferenceId,
        String docName,
        String version,
        LocalDate versionIssueDate,
        boolean isCurrent,
        Integer defaultTrainingValidityDays,
        Instant createdAt
) {
}

