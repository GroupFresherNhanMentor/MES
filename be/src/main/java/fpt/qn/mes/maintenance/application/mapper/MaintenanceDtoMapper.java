package fpt.qn.mes.maintenance.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeDto;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketDto;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

@Mapper(componentModel = "spring")
public interface MaintenanceDtoMapper {

    MaintenanceTicketDto toDto(MaintenanceTicket maintenanceTicket);

    MachineDowntimeDto toDto(MachineDowntime machineDowntime);
}
