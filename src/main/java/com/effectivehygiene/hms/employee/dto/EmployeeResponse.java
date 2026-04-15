package com.effectivehygiene.hms.employee.dto;

import java.time.Instant;

public record EmployeeResponse(
        Long id,
        String employeeNumber,
        String firstName,
        String lastName,
        String department,
        String jobRole,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
