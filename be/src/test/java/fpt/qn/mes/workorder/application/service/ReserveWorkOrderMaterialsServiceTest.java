package fpt.qn.mes.workorder.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.*;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import fpt.qn.mes.workorder.application.port.out.dto.ReservationStock;
import fpt.qn.mes.workorder.application.port.out.WorkOrderReservationPort;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class ReserveWorkOrderMaterialsServiceTest {

    @Mock WorkOrderRepository repository;
    @Mock BomRepository bomRepository;
    @Mock WorkOrderDtoMapper mapper;
    @Mock WarehouseUseCase warehouseUseCase;
    @Mock MachineUseCase machineUseCase;
    @Mock WorkOrderReservationPort reservationPort;
    @Mock AuditLogPort auditLogPort;
    @Mock CurrentUserPort currentUserPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock JsonSerializerPort jsonSerializer;

    @InjectMocks WorkOrderService service;

    UUID workOrderId;
    UUID materialId;
    UUID plannedStatusId;
    UUID readyStatusId;
    UUID availableStatusId;
    UUID reservedStatusId;
    UUID reserveMovementTypeId;
    UUID actorId;
    UUID warehouseId;
    WorkOrder workOrder;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        materialId = UUID.randomUUID();
        plannedStatusId = UUID.randomUUID();
        readyStatusId = UUID.randomUUID();
        availableStatusId = UUID.randomUUID();
        reservedStatusId = UUID.randomUUID();
        reserveMovementTypeId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        workOrder = WorkOrder.builder()
                .id(workOrderId)
                .plannedQuantity(BigDecimal.TEN)
                .workOrderStatusId(plannedStatusId)
                .build();
        when(repository.findForUpdate(workOrderId)).thenReturn(Optional.of(workOrder));
        when(repository.findStatusNameById(plannedStatusId)).thenReturn(Optional.of("PLANNED"));
        when(warehouseUseCase.getWarehouseByCode("RAW_MATERIAL_WAREHOUSE"))
                .thenReturn(WarehouseResponse.builder().id(warehouseId).code("RAW_MATERIAL_WAREHOUSE").build());
        when(reservationPort.findStockStatusId("AVAILABLE")).thenReturn(availableStatusId);
        when(reservationPort.findStockStatusId("RESERVED")).thenReturn(reservedStatusId);
        when(reservationPort.findMovementTypeId("RESERVE")).thenReturn(reserveMovementTypeId);
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);
    }

    @Test
    void reserveMaterials_reservesFifoStockAndMovesWorkOrderToReady() {
        when(repository.findStatusIdByName("READY_TO_PRODUCE")).thenReturn(Optional.of(readyStatusId));
        when(repository.hasActiveTransition(plannedStatusId, readyStatusId)).thenReturn(true);
        when(repository.findMaterialsByWorkOrderId(workOrderId)).thenReturn(List.of(
                WorkOrderMaterial.builder()
                        .workOrderId(workOrderId)
                        .materialProductId(materialId)
                        .requiredQuantity(BigDecimal.valueOf(5))
                        .reservedQuantity(BigDecimal.ZERO)
                        .build()));
        UUID olderLot = UUID.randomUUID();
        UUID newerLot = UUID.randomUUID();
        UUID olderBalance = UUID.randomUUID();
        UUID newerBalance = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        when(reservationPort.findAvailableStock(eq(warehouseId), eq(List.of(materialId)), eq(availableStatusId)))
                .thenReturn(List.of(
                        new ReservationStock(olderBalance, warehouseId, locationId, materialId, olderLot,
                                BigDecimal.valueOf(2), null),
                        new ReservationStock(newerBalance, warehouseId, locationId, materialId, newerLot,
                                BigDecimal.valueOf(5), null)));

        var result = service.reserveMaterials(workOrderId);

        assertEquals(workOrderId, result.getWorkOrderId());
        assertEquals("READY_TO_PRODUCE", result.getStatus());
        verify(reservationPort).applyReservation(eq(workOrderId), any(List.class), any(),
                eq(availableStatusId), eq(reservedStatusId), eq(reserveMovementTypeId), eq(actorId));
        verify(auditLogPort).recordStatusTransition(actorId, workOrderId, "PLANNED", "READY_TO_PRODUCE",
                "RESERVE_MATERIAL");
        verifyNoInteractions(machineUseCase);
    }

    @Test
    void reserveMaterials_shortageUpdatesStatusAndReturnsDetails() {
        UUID warehouseId = UUID.randomUUID();
        when(warehouseUseCase.getWarehouseByCode("RAW_MATERIAL_WAREHOUSE"))
                .thenReturn(WarehouseResponse.builder().id(warehouseId).build());
        when(repository.findMaterialsByWorkOrderId(workOrderId)).thenReturn(List.of(
                WorkOrderMaterial.builder()
                        .workOrderId(workOrderId)
                        .materialProductId(materialId)
                        .requiredQuantity(BigDecimal.TEN)
                        .reservedQuantity(BigDecimal.ZERO)
                        .build()));
        when(reservationPort.findAvailableStock(eq(warehouseId), eq(List.of(materialId)), eq(availableStatusId)))
                .thenReturn(List.of(new ReservationStock(UUID.randomUUID(), warehouseId, UUID.randomUUID(),
                        materialId, UUID.randomUUID(), BigDecimal.ONE, null)));
        UUID shortageStatusId = UUID.randomUUID();
        when(repository.findStatusIdByName("MATERIAL_SHORTAGE")).thenReturn(Optional.of(shortageStatusId));
        when(repository.hasActiveTransition(plannedStatusId, shortageStatusId)).thenReturn(true);

        var exception = assertThrows(InsufficientMaterialException.class,
                () -> service.reserveMaterials(workOrderId));

        assertEquals("INSUFFICIENT_STOCK", exception.getErrorCode().getCode());
        verify(auditLogPort).recordStatusTransition(actorId, workOrderId, "PLANNED", "MATERIAL_SHORTAGE",
                "RESERVE_MATERIAL");
        verify(reservationPort, org.mockito.Mockito.never()).applyReservation(any(), any(), any(), any(), any(), any(), any());
        verifyNoInteractions(machineUseCase);
    }
}
