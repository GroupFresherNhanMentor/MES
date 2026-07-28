package fpt.qn.mes.master.machine.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.MachinesRecord;
import fpt.qn.mes.master.machine.domain.entities.Machine;

@Component
public class MachineRecordMapper {

    public Machine toDomain(MachinesRecord r) {
        if (r == null) return null;
        return Machine.builder()
                .id(r.getId()).productionLineId(r.getProductionLineId())
                .code(r.getCode()).name(r.getName()).machineStatusId(r.getMachineStatusId())
                .createdAt(r.getCreatedAt().toInstant()).createdBy(r.getCreatedBy())
                .updatedAt(r.getUpdatedAt().toInstant()).updatedBy(r.getUpdatedBy())
                .build();
    }

    public MachinesRecord toRecord(Machine m) {
        MachinesRecord r = new MachinesRecord();
        r.setId(m.getId()); r.setProductionLineId(m.getProductionLineId());
        r.setCode(m.getCode()); r.setName(m.getName()); r.setMachineStatusId(m.getMachineStatusId());
        r.setCreatedAt(OffsetDateTime.ofInstant(m.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(m.getCreatedBy());
        r.setUpdatedAt(OffsetDateTime.ofInstant(m.getUpdatedAt(), ZoneOffset.UTC));
        r.setUpdatedBy(m.getUpdatedBy());
        return r;
    }
}
