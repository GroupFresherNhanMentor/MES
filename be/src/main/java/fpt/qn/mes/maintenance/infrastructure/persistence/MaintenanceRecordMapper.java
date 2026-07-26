package fpt.qn.mes.maintenance.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.MachineDowntimesRecord;
import fpt.qn.mes.jooq.tables.records.MaintenanceTicketsRecord;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

@Component
public class MaintenanceRecordMapper {

    public MaintenanceTicket toDomain(MaintenanceTicketsRecord r) {
        return null;
    }

    public MaintenanceTicketsRecord toRecord(MaintenanceTicket t) {
        return null;
    }

    public MachineDowntime toDomain(MachineDowntimesRecord r) {
        return null;
    }

    public MachineDowntimesRecord toRecord(MachineDowntime d) {
        return null;
    }
}
