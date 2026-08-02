package fpt.qn.mes.master.machine.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.MachineStatusesRecord;
import fpt.qn.mes.jooq.tables.records.MachinesRecord;
import fpt.qn.mes.jooq.tables.records.ProductionLinesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.entities.Machine.ProductionLineRef;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;

@Component
public class MachineRecordMapper {

    public Machine toDomain(MachinesRecord r, MachineStatusesRecord status, ProductionLinesRecord line,
            UsersRecord creator, UsersRecord updater) {
        ProductionLineRef lineRef = null;
        if (r.getProductionLineId() != null) {
            lineRef = ProductionLineRef.builder()
                    .id(r.getProductionLineId())
                    .code(line.getId() != null ? line.getCode() : null)
                    .name(line.getId() != null ? line.getName() : null)
                    .build();
        }

        MachineStatus machineStatus = null;
        if (r.getMachineStatusId() != null) {
            machineStatus = MachineStatus.builder()
                    .id(r.getMachineStatusId())
                    .name(status.getId() != null ? status.getName() : null)
                    .description(status.getId() != null ? status.getDescription() : null)
                    .build();
        }

        return Machine.builder()
                .id(r.getId())
                .productionLine(lineRef)
                .code(r.getCode())
                .name(r.getName())
                .machineStatus(machineStatus)
                .createdAt(r.getCreatedAt().toInstant())
                .createdBy(creator.getId() != null
                        ? Machine.UserRef.builder()
                                .id(creator.getId())
                                .fullName(creator.getFullName())
                                .username(creator.getUsername())
                                .build()
                        : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .updatedBy(updater.getId() != null
                        ? Machine.UserRef.builder()
                                .id(updater.getId())
                                .fullName(updater.getFullName())
                                .username(updater.getUsername())
                                .build()
                        : null)
                .build();
    }

    public MachinesRecord toRecord(Machine m) {
        MachinesRecord r = new MachinesRecord();
        r.setId(m.getId());
        r.setProductionLineId(m.getProductionLine() != null ? m.getProductionLine().getId() : null);
        r.setCode(m.getCode());
        r.setName(m.getName());
        r.setMachineStatusId(m.getMachineStatus() != null ? m.getMachineStatus().getId() : null);
        r.setCreatedAt(OffsetDateTime.ofInstant(m.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(m.getCreatedBy() != null ? m.getCreatedBy().getId() : null);
        r.setUpdatedAt(m.getUpdatedAt() != null ? OffsetDateTime.ofInstant(m.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setUpdatedBy(m.getUpdatedBy() != null ? m.getUpdatedBy().getId() : null);
        return r;
    }
}
