package com.effectivehygiene.hms.integration.api;

import com.effectivehygiene.hms.domain.exception.InactiveEntityException;
import com.effectivehygiene.hms.security.SecurityConfig;
import com.effectivehygiene.hms.training.TrainerType;
import com.effectivehygiene.hms.training.TrainingController;
import com.effectivehygiene.hms.training.TrainingInstance;
import com.effectivehygiene.hms.training.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@Import(SecurityConfig.class)
class ApiTrainingInstanceCreateIT extends ApiContractIntegrationTestBase {

    @MockitoBean
    TrainingService trainingService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createTrainingInstance_returns201AndResponseBody() throws Exception {
        TrainingInstance created = new TrainingInstance();
        created.setTrainerName("Alex Foster");
        created.setTrainerType(TrainerType.EMPLOYEE);
        created.setTrainingStartDate(LocalDate.parse("2026-04-10"));
        created.setTrainingEndDate(LocalDate.parse("2026-04-10"));
        created.setTrainingDuration("2h");
        created.setTrainingExpiryDate(LocalDate.parse("2027-04-10"));
        created.setComments("Initial training cycle");
        created.setTrainerSignature("TRN-SIG-500");
        ReflectionTestUtils.setField(created, "id", 500L);
        ReflectionTestUtils.setField(created, "createdAt", Instant.parse("2026-04-10T09:00:00Z"));

        when(trainingService.createTrainingInstance(any())).thenReturn(created);

        mockMvc.perform(post("/api/training/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerName": "Alex Foster",
                                  "trainerType": "EMPLOYEE",
                                  "trainingStartDate": "2026-04-10",
                                  "trainingEndDate": "2026-04-10",
                                  "trainingDuration": "2h",
                                  "trainingExpiryDate": "2027-04-10",
                                  "comments": "Initial training cycle",
                                  "trainerSignature": "TRN-SIG-500",
                                  "employeeIds": [1, 2],
                                  "documentVersionIds": [11, 12]
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.trainerName").value("Alex Foster"))
                .andExpect(jsonPath("$.trainerType").value("EMPLOYEE"))
                .andExpect(jsonPath("$.trainingStartDate").value("2026-04-10"))
                .andExpect(jsonPath("$.trainingEndDate").value("2026-04-10"))
                .andExpect(jsonPath("$.trainingExpiryDate").value("2027-04-10"))
                .andExpect(jsonPath("$.createdAt").value("2026-04-10T09:00:00Z"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createTrainingInstanceWithInactiveEmployee_returns409InactiveEntityContract() throws Exception {
        when(trainingService.createTrainingInstance(any()))
                .thenThrow(new InactiveEntityException("Cannot create training instance: employee ID 2 is inactive"));

        var result = mockMvc.perform(post("/api/training/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerName": "Alex Foster",
                                  "trainerType": "EMPLOYEE",
                                  "trainingStartDate": "2026-04-10",
                                  "trainingEndDate": "2026-04-10",
                                  "trainingDuration": "2h",
                                  "trainingExpiryDate": "2027-04-10",
                                  "comments": "Initial training cycle",
                                  "trainerSignature": "TRN-SIG-500",
                                  "employeeIds": [1, 2],
                                  "documentVersionIds": [11, 12]
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertApiErrorContract(result, 409, "INACTIVE_ENTITY");
    }
}

