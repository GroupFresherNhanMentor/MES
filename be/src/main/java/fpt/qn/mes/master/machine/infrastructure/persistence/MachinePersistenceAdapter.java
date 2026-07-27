package fpt.qn.mes.master.machine.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MACHINES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.MachinesRecord;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachinePersistenceAdapter extends BaseRepository<MachinesRecord> implements MachineRepository {

    MachineRecordMapper mapper;

    public MachinePersistenceAdapter(DSLContext ctx, MachineRecordMapper mapper) {
        super(ctx, MACHINES); this.mapper = mapper;
    }

    @Override public Optional<Machine> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Machine save(Machine m) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Machine update(Machine m) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<Machine> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public boolean existsByCode(String code) { throw new UnsupportedOperationException("Not implemented"); }
}
