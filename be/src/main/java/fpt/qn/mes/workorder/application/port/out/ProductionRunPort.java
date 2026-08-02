package fpt.qn.mes.workorder.application.port.out;

import fpt.qn.mes.workorder.application.port.out.dto.ActiveProductionRun;

import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

public interface ProductionRunPort {

    boolean lockMachine(UUID machineId);

    UUID createProductionRun(UUID workOrderId, UUID machineId, UUID productionLineId, UUID operatorId);

    Optional<UUID> findActiveProductionRunId(UUID workOrderId);

    Optional<ActiveProductionRun> findActiveProductionRunForUpdate(UUID workOrderId);

    boolean closeActiveProductionRun(UUID productionRunId, BigDecimal actualQuantity, BigDecimal goodQuantity,
            BigDecimal defectQuantity, BigDecimal scrapQuantity);

    boolean isMachineRunning(UUID machineId);

    void updateMachineStatus(UUID machineId, String statusName);

    void recordWorkOrderEvent(UUID workOrderId, UUID productionRunId, String eventTypeName, UUID operatorId);

    void recordWorkOrderEvent(UUID workOrderId, UUID productionRunId, String eventTypeName, UUID operatorId, String note);
}
