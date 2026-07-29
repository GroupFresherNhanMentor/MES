package fpt.qn.mes.workorder.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.port.in.WorkOrderUseCase;

@ExtendWith(MockitoExtension.class)
class WorkOrderControllerTest {

    @Mock
    WorkOrderUseCase workOrderUseCase;

    @InjectMocks
    WorkOrderController controller;

    WorkOrderDto sampleDto;
    UUID productId;
    UUID statusId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        statusId = UUID.randomUUID();
        sampleDto = WorkOrderDto.builder()
                .id(UUID.randomUUID())
                .code("WO-2026-0001")
                .finishedProductId(productId)
                .plannedQuantity(BigDecimal.valueOf(100))
                .workOrderStatusId(statusId)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getAll should return 200 OK with ApiResponse containing PageResponse")
    void getAll_shouldReturn200WithApiResponse() {
        // Arrange
        PageResponse<WorkOrderDto> pageResponse = PageResponse.<WorkOrderDto>builder()
                .items(List.of(sampleDto))
                .totalElements(1)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .build();

        when(workOrderUseCase.getWorkOrders(0, 20, productId, statusId, "WO-2026"))
                .thenReturn(pageResponse);

        // Act
        ResponseEntity<ApiResponse<PageResponse<WorkOrderDto>>> response =
                controller.getAll(0, 20, productId, statusId, "WO-2026");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("OK", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getItems().size());
        assertEquals("WO-2026-0001", response.getBody().getData().getItems().getFirst().getCode());

        verify(workOrderUseCase).getWorkOrders(0, 20, productId, statusId, "WO-2026");
    }

    @Test
    @DisplayName("create should return 201 Created with created WorkOrderDto")
    void create_shouldReturn201WithApiResponse() {
        // Arrange
        fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest req =
                new fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest();
        req.setCode("WO-2026-0005");
        req.setFinishedProductId(productId);
        req.setPlannedQuantity(BigDecimal.valueOf(100));

        UUID userId = UUID.randomUUID();
        fpt.qn.mes.auth.application.security.AppUserPrincipal principal =
                fpt.qn.mes.auth.application.security.AppUserPrincipal.builder()
                        .id(userId)
                        .username("planner_user")
                        .roles(List.of("PLANNER"))
                        .build();

        when(workOrderUseCase.createWorkOrder(eq(req), eq(userId))).thenReturn(sampleDto);

        // Act
        ResponseEntity<ApiResponse<WorkOrderDto>> response = controller.create(req, principal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("WO-2026-0001", response.getBody().getData().getCode());

        verify(workOrderUseCase).createWorkOrder(req, userId);
    }

    @Test
    @DisplayName("getById should return 200 OK with WorkOrderDto details")
    void getById_shouldReturn200WithApiResponse() {
        // Arrange
        UUID id = sampleDto.getId();
        when(workOrderUseCase.getWorkOrderById(id)).thenReturn(sampleDto);

        // Act
        ResponseEntity<ApiResponse<WorkOrderDto>> response = controller.getById(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("WO-2026-0001", response.getBody().getData().getCode());

        verify(workOrderUseCase).getWorkOrderById(id);
    }
}
