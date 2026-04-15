package com.effectivehygiene.hms.employee;


import com.effectivehygiene.hms.employee.dto.CreateEmployeeRequest;
import com.effectivehygiene.hms.employee.dto.EmployeeMapper;
import com.effectivehygiene.hms.employee.dto.EmployeeResponse;
import com.effectivehygiene.hms.employee.dto.UpdateEmployeeRequest;
import jakarta.validation.Valid;
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
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee created = employeeService.create(EmployeeMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeMapper.toResponse(created));
    }

    // --------------------
    // READ
    // --------------------

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllActive() {
        return ResponseEntity.ok(EmployeeMapper.toResponseList(employeeService.getAllActive()));
    }

    // --------------------
    // UPDATE
    // --------------------

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        Employee existing = employeeService.getById(id);
        EmployeeMapper.updateEntity(existing, request);
        Employee updated = employeeService.update(existing);
        return ResponseEntity.ok(EmployeeMapper.toResponse(updated));
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
