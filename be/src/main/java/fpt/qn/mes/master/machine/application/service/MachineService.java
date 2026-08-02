package fpt.qn.mes.master.machine.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.application.dto.machine.create.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.machine.search.MachineSearchRequest;
import fpt.qn.mes.master.machine.application.dto.machine.update.UpdateMachineRequest;
import fpt.qn.mes.master.machine.application.exception.MachineConflictException;
import fpt.qn.mes.master.machine.application.exception.MachineNotFoundException;
import fpt.qn.mes.master.machine.application.exception.MachineStatusNotFoundException;
import fpt.qn.mes.master.machine.application.exception.ProductionLineNotFoundException;
import fpt.qn.mes.master.machine.application.mapper.MachineDtoMapper;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.master.machine.application.port.out.ProductionLinePort;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import fpt.qn.mes.master.machine.domain.constants.MachineStatusConstants;
import fpt.qn.mes.master.machine.domain.repository.MachineStatusRepository;
import fpt.qn.mes.master.machine.domain.repository.criteria.MachineSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineService implements MachineUseCase {

    MachineRepository machineRepository;
    MachineStatusRepository machineStatusRepository;
    ProductionLinePort productionLinePort;
    MachineDtoMapper mapper;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MachineResponse> getMachines(MachineSearchRequest request) {
        var criteria = MachineSearchCriteria.builder()
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .productionLineId(request.getProductionLineId())
                .code(request.getCode())
                .name(request.getName())
                .machineStatusId(request.getMachineStatusId())
                .build();
        var result = machineRepository.search(criteria);
        var items = result.getItems().stream().map(m -> mapper.toDto(m)).toList();
        return PageResponse.<MachineResponse>builder()
                .items(items)
                .totalElements(result.getTotal())
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineResponse getMachineById(UUID id) {
        return machineRepository.findById(id)
                .map(m -> mapper.toDto(m))
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailableForReservation(UUID id) {
        machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
        return machineRepository.isAvailableForUpdate(id);
    }

    @Override
    @Transactional
    public void createMachine(CreateMachineRequest request) {
        if (machineRepository.existsByCode(request.getCode())) {
            throw new MachineConflictException("Machine code already exists: " + request.getCode());
        }
        Optional<Boolean> isLineActiveOpt = productionLinePort.isProductionLineActive(request.getProductionLineId());
        if (isLineActiveOpt.isEmpty()) {
            throw new ProductionLineNotFoundException("Production line not found: " + request.getProductionLineId());
        }
        if (Boolean.FALSE.equals(isLineActiveOpt.get())) {
            throw new MachineConflictException("Cannot assign machine to inactive production line: " + request.getProductionLineId());
        }
        var availableStatus = machineStatusRepository.findByName(MachineStatusConstants.AVAILABLE)
                .orElseThrow(() -> new MachineStatusNotFoundException("AVAILABLE status not found in machine_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var machine = Machine.create(
                request.getProductionLineId(), request.getCode(),
                request.getName(), availableStatus.getId(), currentUserId);
        machineRepository.save(machine);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CREATE_MACHINE,
                "MACHINE", machine.getId(), null, jsonSerializer.toJson(machine), null));
    }

    @Override
    @Transactional
    public void updateMachine(UUID id, UpdateMachineRequest request) {
        var existing = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var updated = Machine.update(existing, request.getName(), currentUserId);
        machineRepository.update(updated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.UPDATE_MACHINE,
                "MACHINE", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(updated), null));
    }

    @Override
    @Transactional
    public void changeMachineStatus(UUID id, UUID newStatusId) {
        var existing = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
        if (!machineStatusRepository.existsById(newStatusId)) {
            throw new MachineStatusNotFoundException("Machine status not found: " + newStatusId);
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var updated = Machine.changeStatus(existing, newStatusId, currentUserId);
        machineRepository.update(updated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CHANGE_MACHINE_STATUS,
                "MACHINE", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(updated), null));
    }
}
