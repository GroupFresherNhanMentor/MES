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
import fpt.qn.mes.workorder.application.exception.WorkOrderNotFoundException;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    WorkOrderRepository repository;

    @Mock
    fpt.qn.mes.bom.domain.repository.BomRepository bomRepository;

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

    @Test
    @DisplayName("createWorkOrder with active BOM should create WorkOrder and calculate material requirements")
    void createWorkOrder_withActiveBom_shouldCreateWorkOrderAndMaterials() {
        // Arrange
        UUID bomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        fpt.qn.mes.bom.domain.entities.Bom activeBom = fpt.qn.mes.bom.domain.entities.Bom.builder()
                .id(bomId)
                .finishedProductId(productId)
                .items(List.of(
                        fpt.qn.mes.bom.domain.entities.BomItem.builder()
                                .id(UUID.randomUUID())
                                .materialProductId(UUID.randomUUID())
                                .quantityPerUnit(BigDecimal.valueOf(2))
                                .scrapRate(BigDecimal.valueOf(0.1))
                                .build()
                ))
                .build();

        fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest req =
                new fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest();
        req.setCode("WO-2026-0005");
        req.setFinishedProductId(productId);
        req.setPlannedQuantity(BigDecimal.valueOf(100));

        when(bomRepository.findActiveByFinishedProductId(productId))
                .thenReturn(java.util.Optional.of(activeBom));
        when(repository.save(any(WorkOrder.class))).thenReturn(sampleEntity);
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        WorkOrderDto result = service.createWorkOrder(req, userId);

        // Assert
        assertNotNull(result);
        assertEquals("WO-2026-0001", result.getCode());
        verify(repository).save(any(WorkOrder.class));
        verify(repository).saveMaterial(any(fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial.class));
    }

    @Test
    @DisplayName("createWorkOrder without active BOM should throw BomNotActiveException")
    void createWorkOrder_withoutActiveBom_shouldThrowBomNotActiveException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest req =
                new fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest();
        req.setCode("WO-2026-0006");
        req.setFinishedProductId(productId);
        req.setPlannedQuantity(BigDecimal.valueOf(50));

        when(bomRepository.findActiveByFinishedProductId(productId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                fpt.qn.mes.workorder.application.exception.BomNotActiveException.class,
                () -> service.createWorkOrder(req, userId)
        );
    }

    @Test
    @DisplayName("getWorkOrderById with existing ID should return WorkOrderDto with materials and events")
    void getWorkOrderById_existingId_shouldReturnDto() {
        // Arrange
        UUID id = sampleEntity.getId();
        when(repository.findById(id)).thenReturn(java.util.Optional.of(sampleEntity));
        when(repository.findMaterialsByWorkOrderId(id)).thenReturn(List.of());
        when(repository.findEventsByWorkOrderId(id)).thenReturn(List.of());
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        WorkOrderDto result = service.getWorkOrderById(id);

        // Assert
        assertNotNull(result);
        assertEquals("WO-2026-0001", result.getCode());
        verify(repository).findById(id);
        verify(repository).findMaterialsByWorkOrderId(id);
        verify(repository).findEventsByWorkOrderId(id);
    }

    @Test
    @DisplayName("getWorkOrderById with non-existent ID should throw WorkOrderNotFoundException")
    void getWorkOrderById_nonExistentId_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                WorkOrderNotFoundException.class,
                () -> service.getWorkOrderById(nonExistentId)
        );
        verify(repository).findById(nonExistentId);
    }
}
