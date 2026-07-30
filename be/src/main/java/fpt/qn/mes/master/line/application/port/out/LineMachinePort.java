package fpt.qn.mes.master.line.application.port.out;

import java.util.UUID;

public interface LineMachinePort {
    boolean hasRunningMachines(UUID productionLineId);
}
