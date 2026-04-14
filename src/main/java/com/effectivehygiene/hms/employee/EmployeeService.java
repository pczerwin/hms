package com.effectivehygiene.hms.employee;


import com.effectivehygiene.hms.domain.exception.DuplicateEntityException;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.domain.exception.InactiveEntityException;
import com.effectivehygiene.hms.employee.dto.EmployeeMapper;
import com.effectivehygiene.hms.employee.dto.UpdateEmployeeRequest;
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

            throw new DuplicateEntityException(
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
                .orElseThrow(() -> new EntityNotFoundException(
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

    public Employee update(Long id, UpdateEmployeeRequest request) {

        Employee existing = getById(id);

        if (!existing.isActive()) {
            throw new InactiveEntityException(
                    "Cannot update inactive employee id=" + id
            );
        }

        EmployeeMapper.updateEntity(existing, request);

        if (existing.getEmployeeNumber() != null &&
                employeeRepository.existsByEmployeeNumberAndIdNot(existing.getEmployeeNumber(), id)) {

            throw new DuplicateEntityException(
                    "Employee number already in use: " + existing.getEmployeeNumber()
            );
        }

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
