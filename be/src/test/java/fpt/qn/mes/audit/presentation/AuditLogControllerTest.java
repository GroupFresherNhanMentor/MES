package fpt.qn.mes.audit.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fpt.qn.mes.audit.application.dto.AuditLogResponse;
import fpt.qn.mes.audit.application.dto.AuditLogSearchRequest;
import fpt.qn.mes.audit.application.port.in.AuditLogUseCase;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.common.dto.response.PageResponse;

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuditLogUseCase auditLogUseCase;

    @TestConfiguration
    static class RestTemplateConfig {
        @Bean
        public TestRestTemplate testRestTemplate() {
            return new TestRestTemplate();
        }
    }

    @Test
    @DisplayName("GET /api/audit-logs returns 200 OK with PageResponse")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAuditLogs_ReturnsPageResponse() throws Exception {
        UUID logId = UUID.randomUUID();
        AuditLogResponse dto = AuditLogResponse.builder()
                .id(logId)
                .action(AuditAction.ADJUST_STOCK)
                .entityType("STOCK_BALANCE")
                .createdAt(Instant.now())
                .build();
        PageResponse<AuditLogResponse> pageResponse = PageResponse.of(List.of(dto), 1L, 0, 20);

        when(auditLogUseCase.getAuditLogs(any(AuditLogSearchRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(logId.toString()))
                .andExpect(jsonPath("$.data.items[0].action").value("ADJUST_STOCK"));
    }

    @Test
    @DisplayName("GET /api/audit-logs/{id} returns 200 OK with AuditLogResponse")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAuditLogById_ReturnsAuditLogResponse() throws Exception {
        UUID logId = UUID.randomUUID();
        AuditLogResponse dto = AuditLogResponse.builder()
                .id(logId)
                .action(AuditAction.CREATE_WORK_ORDER)
                .entityType("WORK_ORDER")
                .createdAt(Instant.now())
                .build();

        when(auditLogUseCase.getAuditLogById(logId)).thenReturn(dto);

        mockMvc.perform(get("/api/audit-logs/" + logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(logId.toString()))
                .andExpect(jsonPath("$.data.action").value("CREATE_WORK_ORDER"));
    }
}
