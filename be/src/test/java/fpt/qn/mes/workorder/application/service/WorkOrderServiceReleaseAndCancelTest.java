package fpt.qn.mes.workorder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.InvalidWorkOrderStateException;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.WorkOrderNotFoundException;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import fpt.qn.mes.workorder.application.port.out.WorkOrderReservationPort;
import fpt.qn.mes.workorder.domain.constants.WorkOrderStatusConstants;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceReleaseAndCancelTest {

    @Mock
    WorkOrderRepository repository;

    @Mock
    WorkOrderReservationPort reservationPort;

    @Mock
    WorkOrderDtoMapper mapper;

    @Mock
    AuditLogPort auditLogPort;

    @Mock
    CurrentUserPort currentUserPort;

    @InjectMocks
    WorkOrderService workOrderService;

    UUID workOrderId;
    UUID availableStatusId;
    UUID reservedStatusId;
    UUID releaseMovementTypeId;
    UUID readyStatusId;
    UUID plannedStatusId;
    UUID cancelledStatusId;
    UUID inProgressStatusId;
    UUID actorId;
    WorkOrder mockWorkOrder;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        availableStatusId = UUID.randomUUID();
        reservedStatusId = UUID.randomUUID();
        releaseMovementTypeId = UUID.randomUUID();
        readyStatusId = UUID.randomUUID();
        plannedStatusId = UUID.randomUUID();
        cancelledStatusId = UUID.randomUUID();
        inProgressStatusId = UUID.randomUUID();
        actorId = UUID.randomUUID();

        mockWorkOrder = WorkOrder.builder()
                .id(workOrderId)
                .code("WO-001")
                .plannedQuantity(BigDecimal.valueOf(100))
                .workOrderStatusId(readyStatusId)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("releaseMaterials should release materials and revert READY_TO_PRODUCE status to PLANNED")
    void releaseMaterials_Success() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.READY_TO_PRODUCE));
        when(reservationPort.findStockStatusId("AVAILABLE")).thenReturn(availableStatusId);
        when(reservationPort.findStockStatusId("RESERVED")).thenReturn(reservedStatusId);
        when(reservationPort.findMovementTypeId("RELEASE_RESERVATION")).thenReturn(releaseMovementTypeId);
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(repository.findStatusIdByName(WorkOrderStatusConstants.PLANNED)).thenReturn(Optional.of(plannedStatusId));
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderDto.builder().id(workOrderId).code("WO-001").statusName("PLANNED").build());

        WorkOrderDto result = workOrderService.releaseMaterials(workOrderId);

        assertNotNull(result);
        assertEquals("WO-001", result.getCode());
        verify(reservationPort).releaseReservation(workOrderId, availableStatusId, reservedStatusId, releaseMovementTypeId, actorId);
        verify(auditLogPort).recordStatusTransition(actorId, workOrderId, WorkOrderStatusConstants.READY_TO_PRODUCE, WorkOrderStatusConstants.PLANNED);
    }

    @Test
    @DisplayName("releaseMaterials should throw InvalidWorkOrderStateException if IN_PROGRESS")
    void releaseMaterials_ThrowsException_WhenInProgress() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.IN_PROGRESS));

        assertThrows(InvalidWorkOrderStateException.class, () -> workOrderService.releaseMaterials(workOrderId));
    }

    @Test
    @DisplayName("cancelWorkOrder should release materials and update status to CANCELLED")
    void cancelWorkOrder_Success() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.READY_TO_PRODUCE));
        when(reservationPort.findStockStatusId("AVAILABLE")).thenReturn(availableStatusId);
        when(reservationPort.findStockStatusId("RESERVED")).thenReturn(reservedStatusId);
        when(reservationPort.findMovementTypeId("RELEASE_RESERVATION")).thenReturn(releaseMovementTypeId);
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(repository.findStatusIdByName(WorkOrderStatusConstants.CANCELLED)).thenReturn(Optional.of(cancelledStatusId));
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderDto.builder().id(workOrderId).code("WO-001").statusName("CANCELLED").build());

        WorkOrderDto result = workOrderService.cancelWorkOrder(workOrderId);

        assertNotNull(result);
        assertEquals("WO-001", result.getCode());
        verify(reservationPort).releaseReservation(workOrderId, availableStatusId, reservedStatusId, releaseMovementTypeId, actorId);
        verify(auditLogPort).recordStatusTransition(actorId, workOrderId, WorkOrderStatusConstants.READY_TO_PRODUCE, WorkOrderStatusConstants.CANCELLED);
    }

    @Test
    @DisplayName("cancelWorkOrder should throw InvalidWorkOrderStateException if COMPLETED")
    void cancelWorkOrder_ThrowsException_WhenCompleted() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.COMPLETED));

        assertThrows(InvalidWorkOrderStateException.class, () -> workOrderService.cancelWorkOrder(workOrderId));
    }

    @Test
    @DisplayName("releaseMaterials should throw WorkOrderNotFoundException when ID invalid")
    void releaseMaterials_NotFound() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.empty());

        assertThrows(WorkOrderNotFoundException.class, () -> workOrderService.releaseMaterials(workOrderId));
    }
}
