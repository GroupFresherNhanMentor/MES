package fpt.qn.mes.master.machine.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ProductionLinePort {
    Optional<Boolean> isProductionLineActive(UUID productionLineId);
}
