package fpt.qn.mes.maintenance.application.mapper;

import org.springframework.stereotype.Component;

import fpt.qn.mes.maintenance.application.dto.MachineDowntimeDto;
import fpt.qn.mes.maintenance.application.dto.MaintenanceTicketDto;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

@Component
public class MaintenanceDtoMapper {

    public MaintenanceTicketDto toDto(MaintenanceTicket t) {
        return null;
    }

    public MachineDowntimeDto toDto(MachineDowntime d) {
        return null;
    }
}
