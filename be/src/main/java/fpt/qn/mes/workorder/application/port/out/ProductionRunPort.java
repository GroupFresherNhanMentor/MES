package fpt.qn.mes.workorder.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ProductionRunPort {

    UUID createProductionRun(UUID workOrderId, UUID machineId, UUID productionLineId, UUID operatorId);

    Optional<UUID> findActiveProductionRunId(UUID workOrderId);

    boolean isMachineRunning(UUID machineId);

    void updateMachineStatus(UUID machineId, String statusName);

    void recordWorkOrderEvent(UUID workOrderId, UUID productionRunId, String eventTypeName, UUID operatorId);
}
