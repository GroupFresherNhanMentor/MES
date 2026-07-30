package fpt.qn.mes.workorder.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import fpt.qn.mes.workorder.application.dto.request.ReserveWorkOrderMaterialsRequest;
import fpt.qn.mes.workorder.application.dto.response.ReserveWorkOrderMaterialsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
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
        WorkOrderSearchRequest request = new WorkOrderSearchRequest();
        request.setPage(0);
        request.setSize(20);
        request.setFinishedProductId(productId);
        request.setStatusId(statusId);
        request.setCode("WO-2026");

        PageResponse<WorkOrderDto> pageResponse = PageResponse.<WorkOrderDto>builder()
                .items(List.of(sampleDto))
                .totalElements(1)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .build();

        when(workOrderUseCase.getWorkOrders(any(WorkOrderSearchRequest.class)))
                .thenReturn(pageResponse);

        // Act
        ResponseEntity<ApiResponse<PageResponse<WorkOrderDto>>> response =
                controller.getAll(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("OK", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getItems().size());
        assertEquals("WO-2026-0001", response.getBody().getData().getItems().getFirst().getCode());

        verify(workOrderUseCase).getWorkOrders(any(WorkOrderSearchRequest.class));
    }

    @Test
    @DisplayName("create should return 201 Created with created WorkOrderDto")
    void create_shouldReturn201WithApiResponse() {
        // Arrange
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setCode("WO-2026-0005");
        req.setFinishedProductId(productId);
        req.setPlannedQuantity(BigDecimal.valueOf(100));

        UUID userId = UUID.randomUUID();
        AppUserPrincipal principal = AppUserPrincipal.builder()
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

    @Test
    @DisplayName("update should return 200 OK with updated WorkOrderDto")
    void update_shouldReturn200WithApiResponse() {
        // Arrange
        UUID id = sampleDto.getId();
        fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest req =
                fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest.builder()
                        .code("WO-2026-UPDATED")
                        .build();

        when(workOrderUseCase.updateWorkOrder(eq(id), any())).thenReturn(sampleDto);

        // Act
        ResponseEntity<ApiResponse<WorkOrderDto>> response = controller.update(id, req);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Work Order updated successfully", response.getBody().getMessage());

        verify(workOrderUseCase).updateWorkOrder(eq(id), any());
    }
    @Test
    @DisplayName("reserveMaterials should return 200 OK with ReserveWorkOrderMaterialsResponse")
    void reserveMaterials_shouldReturn200WithApiResponse() {
        // Arrange
        UUID id = sampleDto.getId();
        ReserveWorkOrderMaterialsRequest req =
                new ReserveWorkOrderMaterialsRequest();
        req.setMachineId(UUID.randomUUID());

        ReserveWorkOrderMaterialsResponse expectedResponse =
                ReserveWorkOrderMaterialsResponse.builder()
                        .workOrderId(id)
                        .status("READY_TO_PRODUCE")
                        .build();

        when(workOrderUseCase.reserveMaterials(eq(id), eq(req))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<ReserveWorkOrderMaterialsResponse>> response =
                controller.reserveMaterials(id, req);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Materials reserved successfully. Work Order is now READY_TO_PRODUCE.", response.getBody().getMessage());
        assertEquals(id, response.getBody().getData().getWorkOrderId());

        verify(workOrderUseCase).reserveMaterials(id, req);
    }
}
