package com.effectivehygiene.hms.employee.dto;

import com.effectivehygiene.hms.employee.Employee;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    @Test
    void toEntity_normalizesOptionalAndRequiredFields() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setEmployeeNumber("   ");
        request.setFirstName("  John ");
        request.setLastName(" Doe  ");
        request.setDepartment("   ");
        request.setJobRole("  QA Inspector ");

        Employee employee = EmployeeMapper.toEntity(request);

        assertThat(employee.getEmployeeNumber()).isNull();
        assertThat(employee.getFirstName()).isEqualTo("John");
        assertThat(employee.getLastName()).isEqualTo("Doe");
        assertThat(employee.getDepartment()).isNull();
        assertThat(employee.getJobRole()).isEqualTo("QA Inspector");
    }
}

