package fpt.qn.mes.master.machine.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.machine.application.dto.machinestatus.MachineStatusResponse;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;

@Mapper(componentModel = "spring")
public interface MachineStatusDtoMapper {
    MachineStatusResponse toDto(MachineStatus status);
}
