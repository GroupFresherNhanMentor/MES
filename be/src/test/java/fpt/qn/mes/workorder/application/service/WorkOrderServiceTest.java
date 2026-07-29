package fpt.qn.mes.workorder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.exception.BomNotActiveException;
import fpt.qn.mes.workorder.application.exception.WorkOrderNotFoundException;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    WorkOrderRepository repository;

    @Mock
    BomRepository bomRepository;

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
        WorkOrderSearchRequest request = new WorkOrderSearchRequest();
        request.setPage(0);
        request.setSize(20);
        request.setFinishedProductId(productId);
        request.setStatusId(statusId);
        request.setCode("WO-2026");

        PaginationResult<WorkOrder> paginationResult = PaginationResult.of(List.of(sampleEntity), 1, 0, 20);
        when(repository.findAll(any(WorkOrderSearchCriteria.class))).thenReturn(paginationResult);
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        PageResponse<WorkOrderDto> result = service.getWorkOrders(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalElements());
        assertEquals("WO-2026-0001", result.getItems().getFirst().getCode());
        verify(repository).findAll(any(WorkOrderSearchCriteria.class));
    }

    @Test
    @DisplayName("getWorkOrders default pagination should call repository with null filters")
    void getWorkOrders_defaultParams_shouldCallRepositoryWithNullFilters() {
        // Arrange
        PaginationResult<WorkOrder> paginationResult = PaginationResult.of(List.of(sampleEntity), 1, 0, 20);
        when(repository.findAll(any(WorkOrderSearchCriteria.class))).thenReturn(paginationResult);
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        PageResponse<WorkOrderDto> result = service.getWorkOrders(null);

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
        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(productId)
                .items(List.of(
                        BomItem.builder()
                                .id(UUID.randomUUID())
                                .materialProductId(UUID.randomUUID())
                                .quantityPerUnit(BigDecimal.valueOf(2))
                                .scrapRate(BigDecimal.valueOf(0.1))
                                .build()
                ))
                .build();

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setCode("WO-2026-0005");
        req.setFinishedProductId(productId);
        req.setPlannedQuantity(BigDecimal.valueOf(100));

        when(bomRepository.findActiveByFinishedProductId(productId)).thenReturn(Optional.of(activeBom));
        when(repository.save(any(WorkOrder.class))).thenReturn(sampleEntity);
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        WorkOrderDto result = service.createWorkOrder(req, userId);

        // Assert
        assertNotNull(result);
        assertEquals("WO-2026-0001", result.getCode());
        verify(repository).save(any(WorkOrder.class));
        verify(repository).saveMaterial(any(WorkOrderMaterial.class));
    }

    @Test
    @DisplayName("createWorkOrder without active BOM should throw BomNotActiveException")
    void createWorkOrder_withoutActiveBom_shouldThrowBomNotActiveException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setCode("WO-2026-0006");
        req.setFinishedProductId(productId);
        req.setPlannedQuantity(BigDecimal.valueOf(50));

        when(bomRepository.findActiveByFinishedProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                BomNotActiveException.class,
                () -> service.createWorkOrder(req, userId)
        );
    }

    @Test
    @DisplayName("getWorkOrderById with existing ID should return WorkOrderDto with materials and events")
    void getWorkOrderById_existingId_shouldReturnDto() {
        // Arrange
        UUID id = sampleEntity.getId();
        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
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
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                WorkOrderNotFoundException.class,
                () -> service.getWorkOrderById(nonExistentId)
        );
        verify(repository).findById(nonExistentId);
    }
}
