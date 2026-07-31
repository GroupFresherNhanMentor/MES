package fpt.qn.mes.master.machine.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.domain.entities.Machine;

@Mapper(componentModel = "spring", uses = {MachineStatusDtoMapper.class})
public interface MachineDtoMapper {
    MachineResponse toDto(Machine machine);
}
