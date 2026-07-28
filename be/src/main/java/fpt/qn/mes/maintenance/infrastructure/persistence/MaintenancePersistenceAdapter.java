package fpt.qn.mes.maintenance.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MAINTENANCE_TICKETS;
import static fpt.qn.mes.jooq.tables.MaintenanceTicketStatuses.MAINTENANCE_TICKET_STATUSES;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.maintenance.application.exception.ResourceNotFoundException;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.MaintenanceTicketsRecord;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;
import fpt.qn.mes.maintenance.domain.repository.MaintenanceRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import static fpt.qn.mes.jooq.Tables.MACHINES;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaintenancePersistenceAdapter extends BaseRepository<MaintenanceTicketsRecord> implements MaintenanceRepository {

    MaintenanceRecordMapper mapper;
    DSLContext dslCtx;

    public MaintenancePersistenceAdapter(DSLContext ctx, MaintenanceRecordMapper mapper) {
        super(ctx, MAINTENANCE_TICKETS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override
    public Optional<UUID> findStatusIdByName(String statusName) {
        return dslCtx.select(MAINTENANCE_TICKET_STATUSES.ID)
                .from(MAINTENANCE_TICKET_STATUSES)
                .where(MAINTENANCE_TICKET_STATUSES.NAME.eq(statusName))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public void updateMachineStatus(UUID machineId, UUID statusId) {
        int updatedRows = dslCtx.update(MACHINES)
                .set(MACHINES.MACHINE_STATUS_ID, statusId) // Hãy check lại tên cột chính xác trong bảng machines (thường là MACHINE_STATUS_ID)
                .where(MACHINES.ID.eq(machineId))
                .execute();

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Không tìm thấy máy với ID: " + machineId);
        }
    }

    // Hàm lấy ID trạng thái hiện tại của riêng Ticket đó
    @Override
    public Optional<UUID> findStatusIdByTicketId(UUID ticketId) {
        return dslCtx.select(MAINTENANCE_TICKETS.TICKET_STATUS_ID)
                .from(MAINTENANCE_TICKETS)
                .where(MAINTENANCE_TICKETS.ID.eq(ticketId))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public void updateTicketStatus(UUID ticketId, UUID statusId) {
        dslCtx.update(MAINTENANCE_TICKETS)
                .set(MAINTENANCE_TICKETS.TICKET_STATUS_ID, statusId)
                .where(MAINTENANCE_TICKETS.ID.eq(ticketId))
                .execute();
    }

    @Override
    public Optional<MaintenanceTicket> findById(UUID ticketId) {
        // Query jOOQ và dùng recordMapper để chuyển sang MaintenanceTicket entity
        return dslCtx.selectFrom(MAINTENANCE_TICKETS)
                .where(MAINTENANCE_TICKETS.ID.eq(ticketId))
                .fetchOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UUID> findStatusIdByCode(String statusName) {
        return dslCtx.select(MAINTENANCE_TICKET_STATUSES.ID)
                .from(MAINTENANCE_TICKET_STATUSES)
                .where(MAINTENANCE_TICKET_STATUSES.NAME.eq(statusName))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public void saveStatus(UUID ticketId, UUID statusId) {
        dslCtx.update(MAINTENANCE_TICKETS)
                .set(MAINTENANCE_TICKETS.TICKET_STATUS_ID, statusId)
                .where(MAINTENANCE_TICKETS.ID.eq(ticketId))
                .execute();
    }

    @Override public MaintenanceTicket save(MaintenanceTicket t) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public MaintenanceTicket update(MaintenanceTicket t) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<MaintenanceTicket> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public MachineDowntime saveDowntime(MachineDowntime d) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<MachineDowntime> findDowntimesByTicketId(UUID ticketId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
}
