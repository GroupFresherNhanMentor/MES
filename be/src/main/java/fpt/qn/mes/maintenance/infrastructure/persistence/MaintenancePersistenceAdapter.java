package fpt.qn.mes.maintenance.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.*;
import static fpt.qn.mes.jooq.tables.MaintenanceTicketStatuses.MAINTENANCE_TICKET_STATUSES;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.maintenance.application.dto.request.MaintenanceTicketSearchQuery;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceMetadataResponse;
import fpt.qn.mes.maintenance.application.exception.ResourceNotFoundException;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.MaintenanceTicketsRecord;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;
import fpt.qn.mes.maintenance.domain.repository.MaintenanceRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaintenancePersistenceAdapter extends BaseRepository<MaintenanceTicketsRecord> implements MaintenanceRepository {

    MaintenanceRecordMapper mapper;
    DSLContext dslCtx;

    public MaintenancePersistenceAdapter(DSLContext ctx, MaintenanceRecordMapper mapper) {
        super(ctx, MAINTENANCE_TICKETS);
        this.mapper = mapper;
        this.dslCtx = ctx;
    }

    // =========================================================================
    // 1. LIFECYCLE MANAGEMENT: MAINTENANCE TICKET
    // =========================================================================

    @Override
    public Optional<MaintenanceTicket> findById(UUID ticketId) {
        return dslCtx.selectFrom(MAINTENANCE_TICKETS)
                .where(MAINTENANCE_TICKETS.ID.eq(ticketId))
                .fetchOptional()
                .map(mapper::toDomain);
    }

    @Override
    public MaintenanceTicket save(MaintenanceTicket ticket) {
        // Import tĩnh bảng do jOOQ sinh ra (ví dụ: public.maintenance_tickets hoặc MAINTENANCE_TICKETS)
        // Thay thế đúng tên Table và Column tương ứng trong DB của dự án
        var targetTable = org.jooq.impl.DSL.table("public.maintenance_tickets");

        dslCtx.insertInto(targetTable)
                .set(org.jooq.impl.DSL.field("id"), ticket.getId())
                .set(org.jooq.impl.DSL.field("machine_id"), ticket.getMachineId())
                .set(org.jooq.impl.DSL.field("ticket_type_id"), ticket.getTicketTypeId())
                .set(org.jooq.impl.DSL.field("priority_id"), ticket.getPriorityId())
                .set(org.jooq.impl.DSL.field("description"), ticket.getDescription())
                .set(org.jooq.impl.DSL.field("ticket_status_id"), ticket.getTicketStatusId())
                .set(org.jooq.impl.DSL.field("assigned_engineer_id"), ticket.getAssignedEngineerId())
                .set(org.jooq.impl.DSL.field("created_by"), ticket.getCreatedBy())
                .set(org.jooq.impl.DSL.field("created_at"), ticket.getCreatedAt())
                .execute();

        return ticket;
    }

    @Override
    public void update(MaintenanceTicket ticket) {
        dslCtx.update(MAINTENANCE_TICKETS)
                .set(MAINTENANCE_TICKETS.TICKET_STATUS_ID, ticket.getTicketStatusId())
                .set(MAINTENANCE_TICKETS.ASSIGNED_ENGINEER_ID, ticket.getAssignedEngineerId())
                .where(MAINTENANCE_TICKETS.ID.eq(ticket.getId()))
                .execute();
    }

    // =========================================================================
    // 2. SEARCH & PAGINATION: MAINTENANCE TICKET
    // =========================================================================

    @Override
    public long countAll() {
        return dslCtx.fetchCount(MAINTENANCE_TICKETS);
    }

    @Override
    public List<MaintenanceTicket> findAllPaged(int offset, int limit) {
        return dslCtx.selectFrom(MAINTENANCE_TICKETS)
                .orderBy(MAINTENANCE_TICKETS.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch()
                .map(record -> MaintenanceTicket.builder()
                        .id(record.getId())
                        .machineId(record.getMachineId())
                        .ticketTypeId(record.getTicketTypeId())
                        .priorityId(record.getPriorityId())
                        .description(record.getDescription())
                        .ticketStatusId(record.getTicketStatusId())
                        .assignedEngineerId(record.getAssignedEngineerId())
                        .createdBy(record.getCreatedBy())
                        .createdAt(record.getCreatedAt() != null ? record.getCreatedAt().toInstant() : null)
                        .build()
                );
    }

    @Override
    public long countByCriteria(MaintenanceTicketSearchQuery query) {
        Condition condition = buildSearchCondition(query);
        return dslCtx.fetchCount(MAINTENANCE_TICKETS, condition);
    }

    @Override
    public List<MaintenanceTicket> findByCriteria(MaintenanceTicketSearchQuery query, int offset, int limit) {
        Condition condition = buildSearchCondition(query);

        return dslCtx.selectFrom(MAINTENANCE_TICKETS)
                .where(condition)
                .orderBy(MAINTENANCE_TICKETS.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch()
                .map(record -> MaintenanceTicket.builder()
                        .id(record.getId())
                        .machineId(record.getMachineId())
                        .ticketTypeId(record.getTicketTypeId())
                        .priorityId(record.getPriorityId())
                        .description(record.getDescription())
                        .ticketStatusId(record.getTicketStatusId())
                        .assignedEngineerId(record.getAssignedEngineerId())
                        .createdBy(record.getCreatedBy())
                        .createdAt(record.getCreatedAt() != null ? record.getCreatedAt().toInstant() : null)
                        .build()
                );
    }

    // Helper động xây dựng WHERE Condition
    private Condition buildSearchCondition(MaintenanceTicketSearchQuery query) {
        Condition condition = DSL.noCondition();

        if (query.getMachineId() != null) {
            condition = condition.and(MAINTENANCE_TICKETS.MACHINE_ID.eq(query.getMachineId()));
        }
        if (query.getTicketStatusId() != null) {
            condition = condition.and(MAINTENANCE_TICKETS.TICKET_STATUS_ID.eq(query.getTicketStatusId()));
        }
        if (query.getPriorityId() != null) {
            condition = condition.and(MAINTENANCE_TICKETS.PRIORITY_ID.eq(query.getPriorityId()));
        }
        if (query.getTicketTypeId() != null) {
            condition = condition.and(MAINTENANCE_TICKETS.TICKET_TYPE_ID.eq(query.getTicketTypeId()));
        }
        if (query.getDescription() != null && !query.getDescription().isBlank()) {
            condition = condition.and(MAINTENANCE_TICKETS.DESCRIPTION.containsIgnoreCase(query.getDescription().trim()));
        }
        if (query.getFromDate() != null) {
            condition = condition.and(MAINTENANCE_TICKETS.CREATED_AT.ge(OffsetDateTime.ofInstant(query.getFromDate(), ZoneOffset.UTC)));
        }
        if (query.getToDate() != null) {
            condition = condition.and(MAINTENANCE_TICKETS.CREATED_AT.le(OffsetDateTime.ofInstant(query.getToDate(), ZoneOffset.UTC)));
        }

        return condition;
    }

    // =========================================================================
    // 3. LIFECYCLE & PAGINATION: MACHINE DOWNTIME
    // =========================================================================

    @Override
    public void saveDowntime(MachineDowntime downtime) {
        dslCtx.insertInto(MACHINE_DOWNTIMES)
                .set(MACHINE_DOWNTIMES.ID, downtime.getId())
                .set(MACHINE_DOWNTIMES.TICKET_ID, downtime.getTicketId())
                .set(MACHINE_DOWNTIMES.MACHINE_ID, downtime.getMachineId())
                .set(MACHINE_DOWNTIMES.START_TIME, downtime.getStartTime() != null ? downtime.getStartTime().atOffset(ZoneOffset.UTC) : null)
                .set(MACHINE_DOWNTIMES.END_TIME, downtime.getEndTime() != null ? downtime.getEndTime().atOffset(ZoneOffset.UTC) : null)
                .set(MACHINE_DOWNTIMES.TOTAL_DOWNTIME_MINUTES, downtime.getTotalDowntimeMinutes())
                .set(MACHINE_DOWNTIMES.ROOT_CAUSE, downtime.getRootCause())
                .set(MACHINE_DOWNTIMES.ACTION_TAKEN, downtime.getActionTaken())
                .execute();
    }

    @Override
    public void updateDowntime(MachineDowntime downtime) {
        dslCtx.update(MACHINE_DOWNTIMES)
                .set(MACHINE_DOWNTIMES.END_TIME, downtime.getEndTime() != null ? downtime.getEndTime().atOffset(ZoneOffset.UTC) : null)
                .set(MACHINE_DOWNTIMES.TOTAL_DOWNTIME_MINUTES, downtime.getTotalDowntimeMinutes())
                .set(MACHINE_DOWNTIMES.ROOT_CAUSE, downtime.getRootCause())
                .set(MACHINE_DOWNTIMES.ACTION_TAKEN, downtime.getActionTaken())
                .where(MACHINE_DOWNTIMES.ID.eq(downtime.getId()))
                .execute();
    }

    @Override
    public List<MachineDowntime> findAllDowntimes() {
        return dslCtx.selectFrom(MACHINE_DOWNTIMES)
                .fetch()
                .map(record -> {
                    OffsetDateTime startTime = record.get(MACHINE_DOWNTIMES.START_TIME);
                    OffsetDateTime endTime = record.get(MACHINE_DOWNTIMES.END_TIME);

                    return MachineDowntime.builder()
                            .id(record.get(MACHINE_DOWNTIMES.ID))
                            .ticketId(record.get(MACHINE_DOWNTIMES.TICKET_ID))
                            .machineId(record.get(MACHINE_DOWNTIMES.MACHINE_ID))
                            .startTime(startTime != null ? startTime.toInstant() : null)
                            .endTime(endTime != null ? endTime.toInstant() : null)
                            .totalDowntimeMinutes(record.get(MACHINE_DOWNTIMES.TOTAL_DOWNTIME_MINUTES))
                            .rootCause(record.get(MACHINE_DOWNTIMES.ROOT_CAUSE))
                            .actionTaken(record.get(MACHINE_DOWNTIMES.ACTION_TAKEN))
                            .build();
                });
    }

    @Override
    public Optional<MachineDowntime> findActiveDowntimeByTicketId(UUID ticketId) {
        return dslCtx.selectFrom(MACHINE_DOWNTIMES)
                .where(MACHINE_DOWNTIMES.TICKET_ID.eq(ticketId))
                .and(MACHINE_DOWNTIMES.END_TIME.isNull())
                .fetchOptional()
                .map(record -> {
                    OffsetDateTime startTime = record.get(MACHINE_DOWNTIMES.START_TIME);
                    OffsetDateTime endTime = record.get(MACHINE_DOWNTIMES.END_TIME);

                    return MachineDowntime.builder()
                            .id(record.get(MACHINE_DOWNTIMES.ID))
                            .ticketId(record.get(MACHINE_DOWNTIMES.TICKET_ID))
                            .machineId(record.get(MACHINE_DOWNTIMES.MACHINE_ID))
                            .startTime(startTime != null ? startTime.toInstant() : null)
                            .endTime(endTime != null ? endTime.toInstant() : null)
                            .totalDowntimeMinutes(record.get(MACHINE_DOWNTIMES.TOTAL_DOWNTIME_MINUTES))
                            .rootCause(record.get(MACHINE_DOWNTIMES.ROOT_CAUSE))
                            .actionTaken(record.get(MACHINE_DOWNTIMES.ACTION_TAKEN))
                            .build();
                });
    }

    @Override
    public Optional<MachineDowntime> findDowntimeByTicketId(UUID ticketId) {
        return dslCtx.selectFrom(MACHINE_DOWNTIMES)
                .where(MACHINE_DOWNTIMES.TICKET_ID.eq(ticketId))
                .fetchOptional()
                .map(record -> {
                    OffsetDateTime startTime = record.get(MACHINE_DOWNTIMES.START_TIME);
                    OffsetDateTime endTime = record.get(MACHINE_DOWNTIMES.END_TIME);

                    return MachineDowntime.builder()
                            .id(record.get(MACHINE_DOWNTIMES.ID))
                            .ticketId(record.get(MACHINE_DOWNTIMES.TICKET_ID))
                            .machineId(record.get(MACHINE_DOWNTIMES.MACHINE_ID))
                            .startTime(startTime != null ? startTime.toInstant() : null)
                            .endTime(endTime != null ? endTime.toInstant() : null)
                            .totalDowntimeMinutes(record.get(MACHINE_DOWNTIMES.TOTAL_DOWNTIME_MINUTES))
                            .rootCause(record.get(MACHINE_DOWNTIMES.ROOT_CAUSE))
                            .actionTaken(record.get(MACHINE_DOWNTIMES.ACTION_TAKEN))
                            .build();
                });
    }

    // =========================================================================
    // 4. METADATA QUERIES (STATUS, PRIORITY, TYPE)
    // =========================================================================

    @Override
    public Optional<UUID> findStatusIdByName(String statusName) {
        return dslCtx.select(MAINTENANCE_TICKET_STATUSES.ID)
                .from(MAINTENANCE_TICKET_STATUSES)
                .where(MAINTENANCE_TICKET_STATUSES.NAME.eq(statusName))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public Optional<UUID> findStatusIdByTicketId(UUID ticketId) {
        return dslCtx.select(MAINTENANCE_TICKETS.TICKET_STATUS_ID)
                .from(MAINTENANCE_TICKETS)
                .where(MAINTENANCE_TICKETS.ID.eq(ticketId))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public List<MaintenanceMetadataResponse> findAllTicketStatuses() {
        return dslCtx.select(MAINTENANCE_TICKET_STATUSES.ID, MAINTENANCE_TICKET_STATUSES.NAME, MAINTENANCE_TICKET_STATUSES.DESCRIPTION)
                .from(MAINTENANCE_TICKET_STATUSES)
                .fetch()
                .map(r -> MaintenanceMetadataResponse.builder()
                        .id(r.get(MAINTENANCE_TICKET_STATUSES.ID))
                        .name(r.get(MAINTENANCE_TICKET_STATUSES.NAME))
                        .description(r.get(MAINTENANCE_TICKET_STATUSES.DESCRIPTION))
                        .build());
    }

    @Override
    public List<MaintenanceMetadataResponse> findAllTicketPriorities() {
        return dslCtx.select(MAINTENANCE_TICKET_PRIORITIES.ID, MAINTENANCE_TICKET_PRIORITIES.NAME, MAINTENANCE_TICKET_PRIORITIES.DESCRIPTION)
                .from(MAINTENANCE_TICKET_PRIORITIES)
                .fetch()
                .map(r -> MaintenanceMetadataResponse.builder()
                        .id(r.get(MAINTENANCE_TICKET_PRIORITIES.ID))
                        .name(r.get(MAINTENANCE_TICKET_PRIORITIES.NAME))
                        .description(r.get(MAINTENANCE_TICKET_PRIORITIES.DESCRIPTION))
                        .build());
    }

    @Override
    public List<MaintenanceMetadataResponse> findAllTicketTypes() {
        return dslCtx.select(MAINTENANCE_TICKET_TYPES.ID, MAINTENANCE_TICKET_TYPES.NAME, MAINTENANCE_TICKET_TYPES.DESCRIPTION)
                .from(MAINTENANCE_TICKET_TYPES)
                .fetch()
                .map(r -> MaintenanceMetadataResponse.builder()
                        .id(r.get(MAINTENANCE_TICKET_TYPES.ID))
                        .name(r.get(MAINTENANCE_TICKET_TYPES.NAME))
                        .description(r.get(MAINTENANCE_TICKET_TYPES.DESCRIPTION))
                        .build());
    }

    // =========================================================================
    // 5. EXTERNAL MODULE / SECURITY INTERACTION BOUNDS
    // =========================================================================

    @Override
    public boolean userHasRole(UUID userId, String roleName) {
        return dslCtx.fetchExists(
                dslCtx.selectOne()
                        .from(USER_ROLES)
                        .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                        .where(USER_ROLES.USER_ID.eq(userId))
                        .and(ROLES.NAME.eq(roleName))
        );
    }

    @Override
    public Optional<UUID> findMachineStatusIdByName(String statusName) {
        return dslCtx.select(MACHINE_STATUSES.ID)
                .from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.NAME.eq(statusName))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public void updateMachineStatus(UUID machineId, UUID statusId) {
        int updatedRows = dslCtx.update(MACHINES)
                .set(MACHINES.MACHINE_STATUS_ID, statusId)
                .where(MACHINES.ID.eq(machineId))
                .execute();

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Không tìm thấy máy với ID: " + machineId);
        }
    }
}