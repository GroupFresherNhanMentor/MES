package fpt.qn.mes.bom.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.BOMS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.BomsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomPersistenceAdapter extends BaseRepository<BomsRecord> implements BomRepository {

    BomRecordMapper mapper;
    DSLContext dslCtx;

    public BomPersistenceAdapter(DSLContext ctx, BomRecordMapper mapper) {
        super(ctx, BOMS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override public Optional<Bom> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Bom save(Bom b) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<Bom> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public BomItem saveItem(BomItem item) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Optional<BomItem> findItemById(UUID itemId) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteItemById(UUID itemId) {}
}
