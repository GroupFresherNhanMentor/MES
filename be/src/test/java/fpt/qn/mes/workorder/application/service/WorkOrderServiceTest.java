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
import fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialDto;
import fpt.qn.mes.workorder.application.exception.BomNotActiveException;
import fpt.qn.mes.workorder.application.exception.InvalidInputException;
import fpt.qn.mes.workorder.application.exception.InvalidWorkOrderStateException;
import fpt.qn.mes.workorder.application.exception.WorkOrderCodeExistsException;
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

    @Test
    @DisplayName("updateWorkOrder happy path should update WorkOrder and return DTO")
    void updateWorkOrder_happyPath_shouldUpdateAndReturnDto() {
        // Arrange
        UUID id = sampleEntity.getId();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .code("WO-2026-UPDATED")
                .plannedQuantity(BigDecimal.valueOf(100)) // Same quantity so no recalculation
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));
        when(repository.update(any(WorkOrder.class))).thenReturn(sampleEntity);
        when(repository.findMaterialsByWorkOrderId(id)).thenReturn(List.of());
        when(repository.findEventsByWorkOrderId(id)).thenReturn(List.of());
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        WorkOrderDto result = service.updateWorkOrder(id, req);

        // Assert
        assertNotNull(result);
        verify(repository).update(any(WorkOrder.class));
    }

    @Test
    @DisplayName("updateWorkOrder when quantity changes should recalculate materials")
    void updateWorkOrder_quantityChanged_shouldRecalculateMaterials() {
        // Arrange
        UUID id = sampleEntity.getId();
        UUID bomId = UUID.randomUUID();
        UUID matProductId = UUID.randomUUID();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .plannedQuantity(BigDecimal.valueOf(200))
                .build();

        WorkOrder updatedEntity = WorkOrder.builder()
                .id(id)
                .code(sampleEntity.getCode())
                .finishedProductId(productId)
                .plannedQuantity(BigDecimal.valueOf(200))
                .workOrderStatusId(statusId)
                .build();

        WorkOrderMaterial existingMat = WorkOrderMaterial.builder()
                .id(UUID.randomUUID())
                .workOrderId(id)
                .materialProductId(matProductId)
                .requiredQuantity(BigDecimal.valueOf(200))
                .reservedQuantity(BigDecimal.ZERO)
                .consumedQuantity(BigDecimal.ZERO)
                .build();

        Bom bom = Bom.builder()
                .id(bomId)
                .finishedProductId(productId)
                .items(List.of(
                        BomItem.builder()
                                .id(UUID.randomUUID())
                                .materialProductId(matProductId)
                                .quantityPerUnit(BigDecimal.valueOf(2))
                                .scrapRate(BigDecimal.valueOf(0.1))
                                .build()
                ))
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));
        when(repository.update(any(WorkOrder.class))).thenReturn(updatedEntity);
        when(repository.findMaterialsByWorkOrderId(id)).thenReturn(List.of(existingMat));
        when(bomRepository.findActiveByFinishedProductId(productId)).thenReturn(Optional.of(bom));
        when(repository.findEventsByWorkOrderId(id)).thenReturn(List.of());
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);
        when(mapper.toDto(any(WorkOrderMaterial.class))).thenReturn(WorkOrderMaterialDto.builder().build());

        // Act
        WorkOrderDto result = service.updateWorkOrder(id, req);

        // Assert
        assertNotNull(result);
        verify(repository).updateMaterial(any(WorkOrderMaterial.class));
    }

    @Test
    @DisplayName("updateWorkOrder transition DRAFT to PLANNED should succeed")
    void updateWorkOrder_transitionDraftToPlanned_shouldSucceed() {
        // Arrange
        UUID id = sampleEntity.getId();
        UUID plannedStatusId = UUID.randomUUID();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .workOrderStatusId(plannedStatusId)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));
        when(repository.findStatusNameById(plannedStatusId)).thenReturn(Optional.of("PLANNED"));
        when(repository.update(any(WorkOrder.class))).thenReturn(sampleEntity);
        when(repository.findMaterialsByWorkOrderId(id)).thenReturn(List.of());
        when(repository.findEventsByWorkOrderId(id)).thenReturn(List.of());
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(sampleDto);

        // Act
        WorkOrderDto result = service.updateWorkOrder(id, req);

        // Assert
        assertNotNull(result);
        verify(repository).update(any(WorkOrder.class));
    }

    @Test
    @DisplayName("updateWorkOrder transition DRAFT to IN_PROGRESS via PUT should throw InvalidWorkOrderStateException")
    void updateWorkOrder_transitionToInProgress_shouldThrowException() {
        // Arrange
        UUID id = sampleEntity.getId();
        UUID inProgressStatusId = UUID.randomUUID();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .workOrderStatusId(inProgressStatusId)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));
        when(repository.findStatusNameById(inProgressStatusId)).thenReturn(Optional.of("IN_PROGRESS"));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidWorkOrderStateException.class,
                () -> service.updateWorkOrder(id, req)
        );
    }

    @Test
    @DisplayName("updateWorkOrder with quantity <= 0 should throw InvalidInputException")
    void updateWorkOrder_invalidQuantity_shouldThrowException() {
        // Arrange
        UUID id = sampleEntity.getId();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .plannedQuantity(BigDecimal.ZERO)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidInputException.class,
                () -> service.updateWorkOrder(id, req)
        );
    }

    @Test
    @DisplayName("updateWorkOrder when start date >= end date should throw InvalidInputException")
    void updateWorkOrder_invalidDates_shouldThrowException() {
        // Arrange
        UUID id = sampleEntity.getId();
        Instant now = Instant.now();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .plannedStartDate(now.plusSeconds(86400))
                .plannedEndDate(now)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidInputException.class,
                () -> service.updateWorkOrder(id, req)
        );
    }

    @Test
    @DisplayName("updateWorkOrder on IN_PROGRESS state should throw InvalidWorkOrderStateException")
    void updateWorkOrder_nonEditableState_shouldThrowException() {
        // Arrange
        UUID id = sampleEntity.getId();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .code("WO-2026-NEW")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("IN_PROGRESS"));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                InvalidWorkOrderStateException.class,
                () -> service.updateWorkOrder(id, req)
        );
    }

    @Test
    @DisplayName("updateWorkOrder with duplicate code should throw WorkOrderCodeExistsException")
    void updateWorkOrder_duplicateCode_shouldThrowException() {
        // Arrange
        UUID id = sampleEntity.getId();
        UpdateWorkOrderRequest req = UpdateWorkOrderRequest.builder()
                .code("WO-EXISTING")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(repository.findStatusNameById(statusId)).thenReturn(Optional.of("DRAFT"));
        when(repository.existsByCodeAndIdNot("WO-EXISTING", id)).thenReturn(true);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                WorkOrderCodeExistsException.class,
                () -> service.updateWorkOrder(id, req)
        );
    }
}
