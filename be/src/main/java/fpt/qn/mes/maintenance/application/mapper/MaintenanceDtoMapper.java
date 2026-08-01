package fpt.qn.mes.maintenance.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

@Mapper(componentModel = "spring")
public interface MaintenanceDtoMapper {

    MaintenanceTicketResponse toDto(MaintenanceTicket maintenanceTicket);

    MachineDowntimeResponse toDto(MachineDowntime machineDowntime);
}
