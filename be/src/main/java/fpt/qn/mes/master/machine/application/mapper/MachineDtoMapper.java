package fpt.qn.mes.master.machine.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.machine.application.dto.response.MachineDto;
import fpt.qn.mes.master.machine.domain.entities.Machine;

@Mapper(componentModel = "spring")
public interface MachineDtoMapper {

    MachineDto toDto(Machine machine);
}
