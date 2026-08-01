package fpt.qn.mes.master.machine.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.master.machine.application.dto.machinestatus.MachineStatusResponse;
import fpt.qn.mes.master.machine.application.dto.machinestatus.create.CreateMachineStatusRequest;
import fpt.qn.mes.master.machine.application.dto.machinestatus.search.MachineStatusSearchRequest;
import fpt.qn.mes.master.machine.application.exception.MachineStatusConflictException;
import fpt.qn.mes.master.machine.application.mapper.MachineStatusDtoMapper;
import fpt.qn.mes.master.machine.application.port.in.MachineStatusUseCase;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;
import fpt.qn.mes.master.machine.domain.repository.MachineStatusRepository;
import fpt.qn.mes.master.machine.domain.repository.criteria.MachineStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineStatusService implements MachineStatusUseCase {

    MachineStatusRepository machineStatusRepository;
    MachineStatusDtoMapper machineStatusDtoMapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MachineStatusResponse> getMachineStatuses(MachineStatusSearchRequest request) {
        MachineStatusSearchCriteria criteria = MachineStatusSearchCriteria.builder()
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = machineStatusRepository.search(criteria);
        return PageResponse.<MachineStatusResponse>builder()
                .items(result.getItems().stream().map(s -> machineStatusDtoMapper.toDto(s)).toList())
                .totalElements(result.getTotal())
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createMachineStatus(CreateMachineStatusRequest request) {
        if (machineStatusRepository.existsByName(request.getName())) {
            throw new MachineStatusConflictException("Machine status already exists: " + request.getName());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Instant now = Instant.now();
        machineStatusRepository.save(MachineStatus.builder()
                .id(UuidV7.generate())
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
