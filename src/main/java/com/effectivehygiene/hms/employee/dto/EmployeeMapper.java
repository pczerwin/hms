package com.effectivehygiene.hms.employee.dto;

import com.effectivehygiene.hms.employee.Employee;

import java.util.List;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static Employee toEntity(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setEmployeeNumber(normalizeOptionalText(request.getEmployeeNumber()));
        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setDepartment(normalizeOptionalText(request.getDepartment()));
        employee.setJobRole(normalizeOptionalText(request.getJobRole()));
        return employee;
    }

    public static void updateEntity(Employee employee, UpdateEmployeeRequest request) {
        employee.setEmployeeNumber(normalizeOptionalText(request.getEmployeeNumber()));
        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setDepartment(normalizeOptionalText(request.getDepartment()));
        employee.setJobRole(normalizeOptionalText(request.getJobRole()));
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment(),
                employee.getJobRole(),
                employee.isActive(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    public static List<EmployeeResponse> toResponseList(List<Employee> employees) {
        return employees.stream().map(EmployeeMapper::toResponse).toList();
    }

    private static String normalizeRequiredText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
