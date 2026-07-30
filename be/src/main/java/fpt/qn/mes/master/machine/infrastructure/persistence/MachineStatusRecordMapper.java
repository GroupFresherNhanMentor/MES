package fpt.qn.mes.master.machine.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.MachineStatusesRecord;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;

@Component
public class MachineStatusRecordMapper {

    public MachineStatus toDomain(MachineStatusesRecord r) {
        return MachineStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .createdBy(r.getCreatedBy())
                .updatedBy(r.getUpdatedBy())
                .build();
    }

    public MachineStatusesRecord toRecord(MachineStatus status) {
        MachineStatusesRecord r = new MachineStatusesRecord();
        r.setId(status.getId());
        r.setName(status.getName());
        r.setDescription(status.getDescription());
        r.setCreatedBy(status.getCreatedBy());
        r.setUpdatedBy(status.getUpdatedBy());
        if (status.getCreatedAt() != null) r.setCreatedAt(status.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (status.getUpdatedAt() != null) r.setUpdatedAt(status.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}

