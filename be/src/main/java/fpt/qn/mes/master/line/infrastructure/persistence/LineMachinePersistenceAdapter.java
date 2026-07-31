package fpt.qn.mes.master.line.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MACHINES;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.master.line.application.port.out.LineMachinePort;
import fpt.qn.mes.master.machine.domain.constants.MachineStatusConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineMachinePersistenceAdapter implements LineMachinePort {

    DSLContext ctx;

    @Override
    public boolean hasRunningMachines(UUID productionLineId) {
        return ctx.fetchExists(
                ctx.select()
                        .from(MACHINES)
                        .join(MACHINE_STATUSES).on(MACHINES.MACHINE_STATUS_ID.eq(MACHINE_STATUSES.ID))
                        .where(MACHINES.PRODUCTION_LINE_ID.eq(productionLineId))
                        .and(MACHINE_STATUSES.NAME.eq(MachineStatusConstants.RUNNING)));
    }
}
