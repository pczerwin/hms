package com.effectivehygiene.hms.employee;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // --------------------
    // CREATE
    // --------------------

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        Employee created = employeeService.create(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // --------------------
    // READ
    // --------------------

    @GetMapping
    public ResponseEntity<List<Employee>> getAllActive() {
        return ResponseEntity.ok(employeeService.getAllActive());
    }

    // --------------------
    // UPDATE
    // --------------------

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @RequestBody Employee employee
    ) {
        Employee updated = employeeService.update(id, employee);
        return ResponseEntity.ok(updated);
    }

    // --------------------
    // SOFT DELETE
    // --------------------

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        employeeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

