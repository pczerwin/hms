package com.effectivehygiene.hms.employee;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // --------------------
    // CREATE
    // --------------------

    public Employee create(Employee employee) {

        if (employee.getEmployeeNumber() != null &&
                employeeRepository.existsByEmployeeNumber(employee.getEmployeeNumber())) {

            throw new IllegalStateException(
                    "Employee with number " + employee.getEmployeeNumber() + " already exists"
            );
        }

        return employeeRepository.save(employee);
    }

    // --------------------
    // READ
    // --------------------

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Employee not found: id=" + id
                ));
    }

    public List<Employee> getAllActive() {
        return employeeRepository.findAll()
                .stream()
                .filter(Employee::isActive)
                .toList();
    }

    // --------------------
    // UPDATE
    // --------------------

    public Employee update(Long id, Employee updated) {

        Employee existing = getById(id);

        if (!existing.isActive()) {
            throw new IllegalStateException(
                    "Cannot update inactive employee id=" + id
            );
        }

        if (updated.getEmployeeNumber() != null &&
                !updated.getEmployeeNumber().equals(existing.getEmployeeNumber()) &&
                employeeRepository.existsByEmployeeNumber(updated.getEmployeeNumber())) {

            throw new IllegalStateException(
                    "Employee number already in use: " + updated.getEmployeeNumber()
            );
        }

        existing.setEmployeeNumber(updated.getEmployeeNumber());
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDepartment(updated.getDepartment());
        existing.setJobRole(updated.getJobRole());

        return employeeRepository.save(existing);
    }

    // --------------------
    // SOFT DELETE
    // --------------------

    public void deactivate(Long id) {
        Employee employee = getById(id);

        if (!employee.isActive()) {
            return; // idempotent
        }

        employee.deactivate();
        employeeRepository.save(employee);
    }
}

