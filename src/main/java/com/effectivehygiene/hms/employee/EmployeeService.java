package com.effectivehygiene.hms.employee;


import com.effectivehygiene.hms.domain.exception.DuplicateEntityException;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.domain.exception.InactiveEntityException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee not found: id=" + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllActive() {
        return employeeRepository.findByActiveTrue();
    }

    // --------------------
    // UPDATE
    // --------------------

    public Employee update(Employee employee) {

        if (!employee.isActive()) {
            throw new InactiveEntityException(
                    "Cannot update inactive employee id=" + employee.getId()
            );
        }

        if (employee.getEmployeeNumber() != null &&
                employeeRepository.existsByEmployeeNumberAndIdNot(employee.getEmployeeNumber(), employee.getId())) {

            throw new DuplicateEntityException(
                    "Employee number already in use: " + employee.getEmployeeNumber()
            );
        }

        return employeeRepository.save(employee);
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
