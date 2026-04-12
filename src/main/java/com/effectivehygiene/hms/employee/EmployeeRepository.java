package com.effectivehygiene.hms.employee;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumber(String employeeNumber);
}
