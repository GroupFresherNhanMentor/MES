package fpt.qn.mes.master.machine.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.MachinesRecord;
import fpt.qn.mes.master.machine.domain.entities.Machine;

@Component
public class MachineRecordMapper {

    public Machine toDomain(MachinesRecord r) {
        return null;
    }

    public MachinesRecord toRecord(Machine m) {
        return null;
    }
}
