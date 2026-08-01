package fpt.qn.mes.maintenance.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.MachineDowntimesRecord;
import fpt.qn.mes.jooq.tables.records.MaintenanceTicketsRecord;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

@Component
public class MaintenanceRecordMapper {

    public MaintenanceTicket toDomain(MaintenanceTicketsRecord r) {
        if (r == null) {
            return null;
        }
        return MaintenanceTicket.builder()
                .id(r.getId())
                .machineId(r.getMachineId())
                .ticketTypeId(r.getTicketTypeId())
                .priorityId(r.getPriorityId())
                .description(r.getDescription())
                .ticketStatusId(r.getTicketStatusId())
                .assignedEngineerId(r.getAssignedEngineerId())
                .createdBy(r.getCreatedBy())
                // Convert OffsetDateTime -> Instant
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .build();
    }

    public MaintenanceTicketsRecord toRecord(MaintenanceTicket t) {
        if (t == null) {
            return null;
        }
        MaintenanceTicketsRecord r = new MaintenanceTicketsRecord();
        r.setId(t.getId());
        r.setMachineId(t.getMachineId());
        r.setTicketTypeId(t.getTicketTypeId());
        r.setPriorityId(t.getPriorityId());
        r.setDescription(t.getDescription());
        r.setTicketStatusId(t.getTicketStatusId());
        r.setAssignedEngineerId(t.getAssignedEngineerId());
        r.setCreatedBy(t.getCreatedBy());
        // Convert Instant -> OffsetDateTime
        r.setCreatedAt(t.getCreatedAt() != null ? OffsetDateTime.ofInstant(t.getCreatedAt(), ZoneOffset.UTC) : null);
        return r;
    }

    public MachineDowntime toDomain(MachineDowntimesRecord r) {
        if (r == null) {
            return null;
        }
        return MachineDowntime.builder()
                .id(r.getId())
                .ticketId(r.getTicketId())
                .machineId(r.getMachineId())
                // Convert OffsetDateTime -> Instant
                .startTime(r.getStartTime() != null ? r.getStartTime().toInstant() : null)
                .endTime(r.getEndTime() != null ? r.getEndTime().toInstant() : null)
                .totalDowntimeMinutes(r.getTotalDowntimeMinutes())
                .rootCause(r.getRootCause())
                .actionTaken(r.getActionTaken())
                .build();
    }

    public MachineDowntimesRecord toRecord(MachineDowntime d) {
        if (d == null) {
            return null;
        }
        MachineDowntimesRecord r = new MachineDowntimesRecord();
        r.setId(d.getId());
        r.setTicketId(d.getTicketId());
        r.setMachineId(d.getMachineId());
        // Convert Instant -> OffsetDateTime
        r.setStartTime(d.getStartTime() != null ? OffsetDateTime.ofInstant(d.getStartTime(), ZoneOffset.UTC) : null);
        r.setEndTime(d.getEndTime() != null ? OffsetDateTime.ofInstant(d.getEndTime(), ZoneOffset.UTC) : null);
        r.setTotalDowntimeMinutes(d.getTotalDowntimeMinutes());
        r.setRootCause(d.getRootCause());
        r.setActionTaken(d.getActionTaken());
        return r;
    }
}