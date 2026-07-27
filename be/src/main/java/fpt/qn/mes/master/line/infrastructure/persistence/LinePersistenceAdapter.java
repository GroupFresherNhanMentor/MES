package fpt.qn.mes.master.line.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTION_LINES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.ProductionLinesRecord;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;
import fpt.qn.mes.master.line.domain.repository.ProductionLineRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LinePersistenceAdapter extends BaseRepository<ProductionLinesRecord> implements ProductionLineRepository {

    LineRecordMapper mapper;

    public LinePersistenceAdapter(DSLContext ctx, LineRecordMapper mapper) {
        super(ctx, PRODUCTION_LINES); this.mapper = mapper;
    }

    @Override public Optional<ProductionLine> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public ProductionLine save(ProductionLine l) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public ProductionLine update(ProductionLine l) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<ProductionLine> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public boolean existsByCode(String code) { throw new UnsupportedOperationException("Not implemented"); }
}
