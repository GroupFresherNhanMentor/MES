package fpt.qn.mes.workorder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
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
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.workorder.application.dto.request.StartWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.InvalidWorkOrderStateException;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.MachineNotAvailableException;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.WorkOrderNotFoundException;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import fpt.qn.mes.workorder.application.port.out.ProductionRunPort;
import fpt.qn.mes.workorder.domain.constants.WorkOrderStatusConstants;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceStartPauseResumeTest {

    @Mock
    WorkOrderRepository repository;

    @Mock
    MachineUseCase machineUseCase;

    @Mock
    ProductionRunPort productionRunPort;

    @Mock
    WorkOrderDtoMapper mapper;

    @Mock
    AuditLogPort auditLogPort;

    @Mock
    CurrentUserPort currentUserPort;

    @InjectMocks
    WorkOrderService workOrderService;

    UUID workOrderId;
    UUID machineId;
    UUID lineId;
    UUID runId;
    UUID readyStatusId;
    UUID inProgressStatusId;
    UUID pausedStatusId;
    UUID actorId;
    WorkOrder mockWorkOrder;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        machineId = UUID.randomUUID();
        lineId = UUID.randomUUID();
        runId = UUID.randomUUID();
        readyStatusId = UUID.randomUUID();
        inProgressStatusId = UUID.randomUUID();
        pausedStatusId = UUID.randomUUID();
        actorId = UUID.randomUUID();

        mockWorkOrder = WorkOrder.builder()
                .id(workOrderId)
                .code("WO-START-001")
                .plannedQuantity(BigDecimal.valueOf(100))
                .workOrderStatusId(readyStatusId)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("startWorkOrder should start production successfully when READY_TO_PRODUCE and machine AVAILABLE")
    void startWorkOrder_Success() {
        StartWorkOrderRequest req = StartWorkOrderRequest.builder()
                .machineId(machineId)
                .productionLineId(lineId)
                .build();

        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.READY_TO_PRODUCE));
        when(machineUseCase.isAvailableForReservation(machineId)).thenReturn(true);
        when(productionRunPort.isMachineRunning(machineId)).thenReturn(false);
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(productionRunPort.createProductionRun(workOrderId, machineId, lineId, actorId)).thenReturn(runId);
        when(repository.findStatusIdByName(WorkOrderStatusConstants.IN_PROGRESS)).thenReturn(Optional.of(inProgressStatusId));
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderDto.builder().id(workOrderId).code("WO-START-001").statusName("IN_PROGRESS").build());

        WorkOrderDto result = workOrderService.startWorkOrder(workOrderId, req);

        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatusName());
        verify(productionRunPort).updateMachineStatus(machineId, "RUNNING");
        verify(productionRunPort).recordWorkOrderEvent(workOrderId, runId, "START", actorId);
    }

    @Test
    @DisplayName("startWorkOrder should throw MachineNotAvailableException when machine is not AVAILABLE")
    void startWorkOrder_MachineNotAvailable() {
        StartWorkOrderRequest req = StartWorkOrderRequest.builder()
                .machineId(machineId)
                .productionLineId(lineId)
                .build();

        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.READY_TO_PRODUCE));
        when(machineUseCase.isAvailableForReservation(machineId)).thenReturn(false);

        assertThrows(MachineNotAvailableException.class, () -> workOrderService.startWorkOrder(workOrderId, req));
    }

    @Test
    @DisplayName("startWorkOrder should throw InvalidWorkOrderStateException when machine is running another work order")
    void startWorkOrder_MachineConcurrencyError() {
        StartWorkOrderRequest req = StartWorkOrderRequest.builder()
                .machineId(machineId)
                .productionLineId(lineId)
                .build();

        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.READY_TO_PRODUCE));
        when(machineUseCase.isAvailableForReservation(machineId)).thenReturn(true);
        when(productionRunPort.isMachineRunning(machineId)).thenReturn(true);

        assertThrows(InvalidWorkOrderStateException.class, () -> workOrderService.startWorkOrder(workOrderId, req));
    }

    @Test
    @DisplayName("pauseWorkOrder should pause IN_PROGRESS work order")
    void pauseWorkOrder_Success() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.IN_PROGRESS));
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(productionRunPort.findActiveProductionRunId(workOrderId)).thenReturn(Optional.of(runId));
        when(repository.findStatusIdByName(WorkOrderStatusConstants.PAUSED)).thenReturn(Optional.of(pausedStatusId));
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderDto.builder().id(workOrderId).code("WO-START-001").statusName("PAUSED").build());

        WorkOrderDto result = workOrderService.pauseWorkOrder(workOrderId);

        assertNotNull(result);
        assertEquals("PAUSED", result.getStatusName());
        verify(productionRunPort).recordWorkOrderEvent(workOrderId, runId, "PAUSE", actorId);
    }

    @Test
    @DisplayName("resumeWorkOrder should resume PAUSED work order")
    void resumeWorkOrder_Success() {
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(repository.findStatusNameById(readyStatusId)).thenReturn(Optional.of(WorkOrderStatusConstants.PAUSED));
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
        when(productionRunPort.findActiveProductionRunId(workOrderId)).thenReturn(Optional.of(runId));
        when(repository.findStatusIdByName(WorkOrderStatusConstants.IN_PROGRESS)).thenReturn(Optional.of(inProgressStatusId));
        when(repository.findById(workOrderId)).thenReturn(Optional.of(mockWorkOrder));
        when(mapper.toDto(any(WorkOrder.class))).thenReturn(WorkOrderDto.builder().id(workOrderId).code("WO-START-001").statusName("IN_PROGRESS").build());

        WorkOrderDto result = workOrderService.resumeWorkOrder(workOrderId);

        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatusName());
        verify(productionRunPort).recordWorkOrderEvent(workOrderId, runId, "RESUME", actorId);
    }
}
