package fpt.qn.mes.workorder.application.service;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    WorkOrderRepository repository;

    @Mock
    WorkOrderDtoMapper mapper;

    @InjectMocks
    WorkOrderService service;

    WorkOrder sampleEntity;
    WorkOrderDto sampleDto;
    UUID productId;
    UUID statusId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        statusId = UUID.randomUUID();

        sampleEntity = WorkOrder.builder()
                .id(UUID.randomUUID())
                .code("WO-2026-0001")
                .finishedProductId(productId)
                .plannedQuantity(BigDecimal.valueOf(100))
                .workOrderStatusId(statusId)
                .createdAt(Instant.now())
                .build();

        sampleDto = WorkOrderDto.builder()
                .id(sampleEntity.getId())
                .code(sampleEntity.getCode())
                .finishedProductId(productId)
                .plannedQuantity(BigDecimal.valueOf(100))
                .workOrderStatusId(statusId)
                .createdAt(sampleEntity.getCreatedAt())
                .build();
    }

    @Test
    @DisplayName("getWorkOrders with filters should return mapped PageResponse")
    void getWorkOrders_withFilters_shouldReturnPageResponse() {
        // Arrange
        PaginationResult<WorkOrder> paginationResult = PaginationResult.of(List.of(sampleEntity), 1, 0, 20);
        when(repository.findAll(eq(0), eq(20), eq(productId), eq(statusId), eq("WO-2026")))
                .thenReturn(paginationResult);
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        PageResponse<WorkOrderDto> result = service.getWorkOrders(0, 20, productId, statusId, "WO-2026");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalElements());
        assertEquals("WO-2026-0001", result.getItems().getFirst().getCode());
        verify(repository).findAll(0, 20, productId, statusId, "WO-2026");
    }

    @Test
    @DisplayName("getWorkOrders default pagination should call repository with null filters")
    void getWorkOrders_defaultParams_shouldCallRepositoryWithNullFilters() {
        // Arrange
        PaginationResult<WorkOrder> paginationResult = PaginationResult.of(List.of(sampleEntity), 1, 0, 20);
        when(repository.findAll(eq(0), eq(20), eq(null), eq(null), eq(null)))
                .thenReturn(paginationResult);
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        PageResponse<WorkOrderDto> result = service.getWorkOrders(0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(20, result.getPageSize());
    }
}
