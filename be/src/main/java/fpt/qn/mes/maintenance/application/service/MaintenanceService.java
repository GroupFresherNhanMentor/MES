package fpt.qn.mes.maintenance.application.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.maintenance.application.dto.request.CreateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.request.StartMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.request.CloseMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import fpt.qn.mes.maintenance.application.exception.BusinessException;
import fpt.qn.mes.maintenance.application.exception.MaintenanceTicketNotFoundException;
import fpt.qn.mes.maintenance.application.exception.ResourceNotFoundException;
import fpt.qn.mes.maintenance.application.mapper.MaintenanceDtoMapper;
import fpt.qn.mes.maintenance.application.port.in.MaintenanceUseCase;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;
import fpt.qn.mes.maintenance.domain.repository.MaintenanceRepository;
import fpt.qn.mes.user.domain.repository.UserRepository;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import fpt.qn.mes.master.machine.domain.entities.Machine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class MaintenanceService implements MaintenanceUseCase {

    MaintenanceRepository repository;
    UserRepository userRepository;
    MachineRepository machineRepository;
    CurrentUserPort currentUserPort;
    MaintenanceDtoMapper mapper;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

    @Override
    @Transactional
    public MaintenanceTicketResponse createTicket(CreateMaintenanceTicketRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        UUID openStatusId = repository.findStatusIdByName("OPEN")
                .orElseThrow(() -> new ResourceNotFoundException("Ticket status 'OPEN' does not exist in the system"));

        UUID downMachineStatusId = repository.findMachineStatusIdByName("DOWN")
                .orElseThrow(() -> new ResourceNotFoundException("Machine status 'DOWN' does not exist in the system"));

        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new BusinessException("Machine not found with ID: " + request.getMachineId()));

        Machine updatedMachine = Machine.changeStatus(machine, downMachineStatusId, currentUserId);

        machineRepository.update(updatedMachine);

        MaintenanceTicket ticket = MaintenanceTicket.builder()
                .id(UUID.randomUUID())
                .machineId(request.getMachineId())
                .ticketTypeId(request.getTicketTypeId())
                .priorityId(request.getPriorityId())
                .description(request.getDescription())
                .ticketStatusId(openStatusId)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .build();

        MachineDowntime downtime = MachineDowntime.create(ticket.getId(), ticket.getMachineId(), Instant.now());
        repository.save(ticket);
        repository.saveDowntime(downtime);

        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CREATE_MAINTENANCE_TICKET,
                "MAINTENANCE_TICKET", ticket.getId(),
                null, jsonSerializer.toJson(Map.of("machineId", ticket.getMachineId(), "status", "OPEN")), null));

        return mapper.toDto(ticket);
    }

    @Override
    public MaintenanceTicketResponse startTicket(UUID ticketId, StartMaintenanceTicketRequest request) {
        UUID engineerId = request.getAssignedEngineerId();

        if (engineerId == null) {
            throw new BusinessException("Assigned engineer ID cannot be null");
        }

        userRepository.findById(engineerId)
                .orElseThrow(() -> new BusinessException("The assigned maintenance engineer does not exist"));

        if (!repository.userHasRole(engineerId, "MAINTENANCE_ENGINEER")) {
            throw new BusinessException("The selected user does not have the maintenance engineer role (MAINTENANCE_ENGINEER)");
        }

        UUID openStatusId = repository.findStatusIdByName("OPEN")
                .orElseThrow(() -> new ResourceNotFoundException("Status 'OPEN' does not exist in the system"));

        UUID inProgressStatusId = repository.findStatusIdByName("IN_PROGRESS")
                .orElseThrow(() -> new ResourceNotFoundException("Status 'IN_PROGRESS' does not exist in the system"));

        UUID underMaintenanceStatusId = repository.findMachineStatusIdByName("UNDER_MAINTENANCE")
                .orElseThrow(() -> new ResourceNotFoundException("Machine status 'UNDER_MAINTENANCE' does not exist in the system"));

        MaintenanceTicket ticket = repository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Maintenance ticket not found with ID: " + ticketId));

        Machine machine = machineRepository.findById(ticket.getMachineId())
                .orElseThrow(() -> new BusinessException("Machine associated with the ticket not found"));

        Machine updatedMachine = Machine.changeStatus(machine, underMaintenanceStatusId, engineerId);

        // Đã sửa: Truyền đúng thực thể updatedMachine vào hàm update
        machineRepository.update(updatedMachine);

        ticket.start(openStatusId, inProgressStatusId, engineerId);
        repository.update(ticket);

        eventPublisher.publishEvent(AuditEvent.create(engineerId, AuditAction.START_MAINTENANCE,
                "MAINTENANCE_TICKET", ticketId,
                jsonSerializer.toJson(Map.of("status", "OPEN")),
                jsonSerializer.toJson(Map.of("status", "IN_PROGRESS", "engineerId", engineerId.toString())), null));

        return mapper.toDto(ticket);
    }

    @Override
    public void resolveTicket(UUID ticketId) {
        UUID inProgressStatusId = repository.findStatusIdByName("IN_PROGRESS")
                .orElseThrow(() -> new ResourceNotFoundException("Status 'IN_PROGRESS' does not exist in the system"));

        UUID resolvedStatusId = repository.findStatusIdByName("RESOLVED")
                .orElseThrow(() -> new ResourceNotFoundException("Status 'RESOLVED' does not exist in the system"));

        MaintenanceTicket ticket = repository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Maintenance ticket not found with ID: " + ticketId));

        ticket.resolve(inProgressStatusId, resolvedStatusId);
        repository.update(ticket);

        UUID currentUserId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.RESOLVE_MAINTENANCE_TICKET,
                "MAINTENANCE_TICKET", ticketId,
                jsonSerializer.toJson(Map.of("status", "IN_PROGRESS")),
                jsonSerializer.toJson(Map.of("status", "RESOLVED")), null));
    }

    @Override
    public void cancelTicket(UUID ticketId) {
        UUID openStatusId = repository.findStatusIdByName("OPEN")
                .orElseThrow(() -> new BusinessException("Status 'OPEN' does not exist"));
        UUID cancelledStatusId = repository.findStatusIdByName("CANCELLED")
                .orElseThrow(() -> new BusinessException("Status 'CANCELLED' does not exist"));

        MaintenanceTicket ticket = repository.findById(ticketId)
                .orElseThrow(() -> new MaintenanceTicketNotFoundException("Maintenance ticket not found"));

        ticket.cancel(openStatusId, cancelledStatusId);
        repository.save(ticket);

        UUID currentUserId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CANCEL_MAINTENANCE_TICKET,
                "MAINTENANCE_TICKET", ticketId,
                jsonSerializer.toJson(Map.of("status", "OPEN")),
                jsonSerializer.toJson(Map.of("status", "CANCELLED")), null));
    }

    @Override
    public void closeTicket(CloseMaintenanceTicketRequest request) {
        if (request.getActionTaken() == null || request.getActionTaken().isBlank()) {
            throw new BusinessException("Action taken is required to close the maintenance ticket");
        }

        UUID resolvedStatusId = repository.findStatusIdByName("RESOLVED")
                .orElseThrow(() -> new ResourceNotFoundException("Ticket status 'RESOLVED' does not exist in the system"));

        UUID closedStatusId = repository.findStatusIdByName("CLOSED")
                .orElseThrow(() -> new ResourceNotFoundException("Ticket status 'CLOSED' does not exist in the system"));

        MaintenanceTicket ticket = repository.findById(request.getTicketId())
                .orElseThrow(() -> new BusinessException("Maintenance ticket not found with ID: " + request.getTicketId()));

        ticket.close(resolvedStatusId, closedStatusId);
        repository.update(ticket);

        MachineDowntime downtime = repository.findActiveDowntimeByTicketId(ticket.getId())
                .orElseThrow(() -> new BusinessException("No active downtime record found for this ticket"));

        downtime.endDowntime(Instant.now(), request.getRootCause(), request.getActionTaken());
        repository.updateDowntime(downtime);

        String targetMachineStatus = "AVAILABLE".equalsIgnoreCase(request.getMachineResolutionStatus()) ? "AVAILABLE" : "DOWN";

        UUID targetMachineStatusId = repository.findMachineStatusIdByName(targetMachineStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Machine status '" + targetMachineStatus + "' does not exist in the system"));

        Machine machine = machineRepository.findById(ticket.getMachineId())
                .orElseThrow(() -> new BusinessException("Machine associated with the ticket not found"));

        UUID currentUserId = currentUserPort.getCurrentUserId();
        Machine updatedMachine = Machine.changeStatus(machine, targetMachineStatusId, currentUserId);
        machineRepository.update(updatedMachine);

        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CLOSE_MAINTENANCE_TICKET,
                "MAINTENANCE_TICKET", request.getTicketId(),
                jsonSerializer.toJson(Map.of("status", "RESOLVED")),
                jsonSerializer.toJson(Map.of("status", "CLOSED", "actionTaken", request.getActionTaken())), null));
    }
}