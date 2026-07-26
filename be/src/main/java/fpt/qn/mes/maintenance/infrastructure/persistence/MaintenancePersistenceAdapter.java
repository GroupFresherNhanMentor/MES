package fpt.qn.mes.maintenance.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MAINTENANCE_TICKETS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.PaginationResult;
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
        super(ctx, MAINTENANCE_TICKETS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override public Optional<MaintenanceTicket> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public MaintenanceTicket save(MaintenanceTicket t) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public MaintenanceTicket update(MaintenanceTicket t) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<MaintenanceTicket> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public MachineDowntime saveDowntime(MachineDowntime d) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<MachineDowntime> findDowntimesByTicketId(UUID ticketId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
}
