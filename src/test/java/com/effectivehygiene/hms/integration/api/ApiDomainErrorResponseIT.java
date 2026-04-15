package com.effectivehygiene.hms.integration.api;

import com.effectivehygiene.hms.document.DocumentController;
import com.effectivehygiene.hms.document.DocumentService;
import com.effectivehygiene.hms.domain.exception.DuplicateEntityException;
import com.effectivehygiene.hms.domain.exception.EntityNotFoundException;
import com.effectivehygiene.hms.domain.exception.InactiveEntityException;
import com.effectivehygiene.hms.domain.exception.MissingCurrentVersionException;
import com.effectivehygiene.hms.employee.Employee;
import com.effectivehygiene.hms.employee.EmployeeController;
import com.effectivehygiene.hms.employee.EmployeeService;
import com.effectivehygiene.hms.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EmployeeController.class, DocumentController.class})
@Import(SecurityConfig.class)
class ApiDomainErrorResponseIT extends ApiContractIntegrationTestBase {

    @MockitoBean
    EmployeeService employeeService;

    @MockitoBean
    DocumentService documentService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateInactiveEmployee_returns409InactiveEntityContract() throws Exception {
        Employee existing = new Employee();
        existing.setFirstName("John");
        existing.setLastName("Doe");

        when(employeeService.getById(1L)).thenReturn(existing);
        when(employeeService.update(any(Employee.class))).thenThrow(new InactiveEntityException("Cannot update inactive employee id=1"));

        var result = mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNumber": "1001",
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "department": "Hygiene",
                                  "jobRole": "Supervisor"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 409, "INACTIVE_ENTITY");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createEmployeeDuplicateNumber_returns409DuplicateEntityContract() throws Exception {
        when(employeeService.create(any(Employee.class))).thenThrow(new DuplicateEntityException("Employee with number 1001 already exists"));

        var result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNumber": "1001",
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "department": "Hygiene",
                                  "jobRole": "Supervisor"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 409, "DUPLICATE_ENTITY");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateMissingEmployee_returns404EntityNotFoundContract() throws Exception {
        when(employeeService.getById(999L)).thenThrow(new EntityNotFoundException("Employee not found: id=999"));

        var result = mockMvc.perform(put("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNumber": "1001",
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "department": "Hygiene",
                                  "jobRole": "Supervisor"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 404, "ENTITY_NOT_FOUND");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createDocumentReferenceDuplicate_returns409DuplicateEntityContract() throws Exception {
        when(documentService.createReference(any())).thenThrow(new DuplicateEntityException("Document reference already exists: SOP-001"));

        var result = mockMvc.perform(post("/api/documents/references")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "referenceCode": "SOP-001",
                                  "originDepartment": "Hygiene",
                                  "mandatory": true
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 409, "DUPLICATE_ENTITY");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMissingCurrentVersion_returns409MissingCurrentVersionContract() throws Exception {
        when(documentService.getCurrentVersion(1L)).thenThrow(new MissingCurrentVersionException("No current version exists for document reference: 1"));

        var result = mockMvc.perform(get("/api/documents/references/1/versions/current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 409, "MISSING_CURRENT_VERSION");
    }
}

