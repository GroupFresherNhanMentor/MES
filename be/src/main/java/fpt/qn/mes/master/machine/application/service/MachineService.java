package fpt.qn.mes.master.machine.application.service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.request.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.request.UpdateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.response.MachineDto;
import fpt.qn.mes.master.machine.application.exception.MachineConflictException;
import fpt.qn.mes.master.machine.application.exception.MachineNotFoundException;
import fpt.qn.mes.master.machine.application.mapper.MachineDtoMapper;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static fpt.qn.mes.jooq.Tables.LINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_LINES;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineService implements MachineUseCase {

    MachineRepository machineRepository;
    MachineDtoMapper mapper;
    DSLContext ctx;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MachineDto> getMachines(int page, int size) {
        UUID activeStatusId = getActiveStatusId();
        return getMachinesByStatus(page, size, activeStatusId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MachineDto> getMachinesByStatus(int page, int size, UUID statusId) {
        var result = machineRepository.findAllByStatus(page, size, statusId);
        var items = result.getItems().stream().map(mapper::toDto).toList();
        return PageResponse.<MachineDto>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(page).pageSize(size)
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineDto getMachineById(UUID id) {
        return machineRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
    }

    @Override
    @Transactional
    public boolean isAvailableForReservation(UUID id) {
        machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
        return machineRepository.isAvailableForUpdate(id);
    }

    @Override
    @Transactional
    public MachineDto createMachine(CreateMachineRequest request, UUID currentUserId) {
        if (machineRepository.existsByCode(request.getCode())) {
            throw new MachineConflictException("Machine code already exists: " + request.getCode());
        }
        UUID activeLineStatusId = ctx.select(LINE_STATUSES.ID)
                .from(LINE_STATUSES).where(LINE_STATUSES.NAME.eq("ACTIVE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("ACTIVE not found in line_statuses"));
        UUID lineStatusId = ctx.select(PRODUCTION_LINES.LINE_STATUS_ID)
                .from(PRODUCTION_LINES).where(PRODUCTION_LINES.ID.eq(request.getProductionLineId()))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalArgumentException("Production line not found: " + request.getProductionLineId()));
        if (!activeLineStatusId.equals(lineStatusId)) {
            throw new MachineConflictException("Cannot assign machine to INACTIVE production line");
        }
        var machine = Machine.create(request.getProductionLineId(), request.getCode(),
                request.getName(), request.getMachineStatusId(), currentUserId);
        return mapper.toDto(machineRepository.save(machine));
    }

    @Override
    @Transactional
    public MachineDto updateMachine(UUID id, UpdateMachineRequest request, UUID currentUserId) {
        var existing = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));
        var updated = Machine.builder()
                .id(existing.getId()).productionLineId(existing.getProductionLineId()).code(existing.getCode())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .machineStatusId(request.getMachineStatusId() != null ? request.getMachineStatusId() : existing.getMachineStatusId())
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(currentUserId)
                .build();
        return mapper.toDto(machineRepository.update(updated));
    }

    @Override
    @Transactional
    public void deleteMachine(UUID id) {
        var existing = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));

        boolean isRunning = existing.getMachineStatusId().equals(
                ctx.select(MACHINE_STATUSES.ID).from(MACHINE_STATUSES)
                        .where(MACHINE_STATUSES.NAME.eq("RUNNING"))
                        .fetchOptionalInto(UUID.class).orElse(null));
        if (isRunning) {
            throw new MachineConflictException("Cannot deactivate machine — currently RUNNING: " + id);
        }

        UUID inactiveStatusId = ctx.select(MACHINE_STATUSES.ID)
                .from(MACHINE_STATUSES).where(MACHINE_STATUSES.NAME.eq("RETIRED"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("RETIRED not found in machine_statuses"));

        var deactivated = Machine.builder()
                .id(existing.getId()).productionLineId(existing.getProductionLineId()).code(existing.getCode())
                .name(existing.getName()).machineStatusId(inactiveStatusId)
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(existing.getUpdatedBy())
                .build();
        machineRepository.update(deactivated);
    }

    @Override
    @Transactional
    public MachineDto changeMachineStatus(UUID id, UUID newStatusId, UUID currentUserId) {
        var existing = machineRepository.findById(id)
                .orElseThrow(() -> new MachineNotFoundException("Machine not found: " + id));

        var currentName = ctx.select(MACHINE_STATUSES.NAME).from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.ID.eq(existing.getMachineStatusId()))
                .fetchOptionalInto(String.class)
                .orElse("UNKNOWN");
        var newName = ctx.select(MACHINE_STATUSES.NAME).from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.ID.eq(newStatusId))
                .fetchOptionalInto(String.class)
                .orElseThrow(() -> new fpt.qn.mes.common.exception.DomainException("Invalid status ID: " + newStatusId));

        if (!isValidTransition(currentName, newName)) {
            throw new fpt.qn.mes.common.exception.DomainException(
                    "Invalid machine status transition: " + currentName + " → " + newName);
        }

        var updated = Machine.builder()
                .id(existing.getId()).productionLineId(existing.getProductionLineId())
                .code(existing.getCode()).name(existing.getName())
                .machineStatusId(newStatusId)
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(currentUserId)
                .build();
        return mapper.toDto(machineRepository.update(updated));
    }

    private boolean isValidTransition(String from, String to) {
        return switch (from) {
            case "AVAILABLE" -> Set.of("RUNNING", "UNDER_MAINTENANCE", "RETIRED").contains(to);
            case "RUNNING" -> Set.of("AVAILABLE", "DOWN").contains(to);
            case "DOWN" -> Set.of("UNDER_MAINTENANCE").contains(to);
            case "UNDER_MAINTENANCE" -> Set.of("AVAILABLE", "DOWN").contains(to);
            case "RETIRED" -> false;
            default -> false;
        };
    }

    private UUID getActiveStatusId() {
        return ctx.select(MACHINE_STATUSES.ID).from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.NAME.eq("AVAILABLE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("AVAILABLE not found in machine_statuses"));
    }
}
