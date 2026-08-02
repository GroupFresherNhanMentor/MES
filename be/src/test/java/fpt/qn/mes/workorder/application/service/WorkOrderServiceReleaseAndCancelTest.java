package fpt.qn.mes.workorder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
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

import org.springframework.context.ApplicationEventPublisher;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderResponse;
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

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    JsonSerializerPort jsonSerializer;

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
        when(reservationPort.releaseReservation(workOrderId, availableStatusId, reservedStatusId,
                releaseMovementTypeId, actorId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(repository.findStatusIdByName(WorkOrderStatusConstants.PLANNED)).thenReturn(Optional.of(plannedStatusId));
        when(repository.hasActiveTransition(readyStatusId, plannedStatusId)).thenReturn(true);
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderResponse.builder().id(workOrderId).code("WO-001").workOrderStatusId(plannedStatusId).build());

        WorkOrderResponse result = workOrderService.releaseMaterials(workOrderId);

        assertNotNull(result);
        assertEquals("WO-001", result.getCode());
        verify(reservationPort).releaseReservation(workOrderId, availableStatusId, reservedStatusId, releaseMovementTypeId, actorId);
        verify(auditLogPort).recordStatusTransition(actorId, workOrderId,
                WorkOrderStatusConstants.READY_TO_PRODUCE, WorkOrderStatusConstants.PLANNED, "RELEASE_RESERVATION");
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
        when(reservationPort.releaseReservation(workOrderId, availableStatusId, reservedStatusId,
                releaseMovementTypeId, actorId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(repository.findStatusIdByName(WorkOrderStatusConstants.CANCELLED)).thenReturn(Optional.of(cancelledStatusId));
        when(repository.hasActiveTransition(readyStatusId, cancelledStatusId)).thenReturn(true);
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderResponse.builder().id(workOrderId).code("WO-001").workOrderStatusId(cancelledStatusId).build());

        WorkOrderResponse result = workOrderService.cancelWorkOrder(workOrderId);

        assertNotNull(result);
        assertEquals("WO-001", result.getCode());
        verify(reservationPort).releaseReservation(workOrderId, availableStatusId, reservedStatusId, releaseMovementTypeId, actorId);
        verify(auditLogPort).recordStatusTransition(actorId, workOrderId,
                WorkOrderStatusConstants.READY_TO_PRODUCE, WorkOrderStatusConstants.CANCELLED,
                "RELEASE_RESERVATION");
    }

    @Test
    @DisplayName("cancelWorkOrder should throw InvalidWorkOrderStateException if COMPLETED")
    void cancelWorkOrder_ThrowsException_WhenCompleted() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.COMPLETED));

        assertThrows(InvalidWorkOrderStateException.class, () -> workOrderService.cancelWorkOrder(workOrderId));
    }

    @Test
    @DisplayName("cancelWorkOrder should reject DRAFT when no active transition exists")
    void cancelWorkOrder_ThrowsException_WhenDraftTransitionIsInactive() {
        UUID draftStatusId = UUID.randomUUID();
        WorkOrder draftWorkOrder = WorkOrder.builder()
                .id(workOrderId)
                .code("WO-DRAFT")
                .plannedQuantity(BigDecimal.TEN)
                .workOrderStatusId(draftStatusId)
                .createdAt(Instant.now())
                .build();
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(draftWorkOrder));
        when(repository.findStatusNameById(draftStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.DRAFT));
        when(repository.findStatusIdByName(WorkOrderStatusConstants.CANCELLED)).thenReturn(Optional.of(cancelledStatusId));
        when(repository.hasActiveTransition(draftStatusId, cancelledStatusId)).thenReturn(false);

        assertThrows(InvalidWorkOrderStateException.class, () -> workOrderService.cancelWorkOrder(workOrderId));

        verify(reservationPort, never()).releaseReservation(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("releaseMaterials should throw WorkOrderNotFoundException when ID invalid")
    void releaseMaterials_NotFound() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.empty());

        assertThrows(WorkOrderNotFoundException.class, () -> workOrderService.releaseMaterials(workOrderId));
    }
}
