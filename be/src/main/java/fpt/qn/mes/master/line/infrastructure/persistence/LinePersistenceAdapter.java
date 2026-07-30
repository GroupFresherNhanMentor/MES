package fpt.qn.mes.master.line.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTION_LINES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
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

    @Override
    public Optional<ProductionLine> findById(UUID id) { return fetchById(id).map(mapper::toDomain); }
    @Override
    public ProductionLine save(ProductionLine l) { return mapper.toDomain(create(mapper.toRecord(l))); }
    @Override
    public ProductionLine update(ProductionLine l) { return mapper.toDomain(update(mapper.toRecord(l))); }
    @Override
    public void deleteById(UUID id) {}

    @Override
    public PaginationResult<ProductionLine> findAll(int page, int size) {
        var records = ctx.selectFrom(PRODUCTION_LINES).orderBy(PRODUCTION_LINES.CREATED_AT.desc())
                .limit(size).offset((long) page * size).fetch();
        int total = ctx.fetchCount(ctx.selectFrom(PRODUCTION_LINES));
        return PaginationResult.<ProductionLine>builder().total(total).items(records.stream().map(r -> mapper.toDomain(r)).toList()).build();
    }

    @Override
    public PaginationResult<ProductionLine> findAllByStatus(int page, int size, UUID statusId) {
        var records = ctx.selectFrom(PRODUCTION_LINES)
                .where(PRODUCTION_LINES.LINE_STATUS_ID.eq(statusId))
                .orderBy(PRODUCTION_LINES.CREATED_AT.desc())
                .limit(size).offset((long) page * size).fetch();
        int total = ctx.fetchCount(ctx.selectFrom(PRODUCTION_LINES)
                .where(PRODUCTION_LINES.LINE_STATUS_ID.eq(statusId)));
        return PaginationResult.<ProductionLine>builder().total(total).items(records.stream().map(r -> mapper.toDomain(r)).toList()).build();
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(ctx.selectFrom(PRODUCTION_LINES).where(PRODUCTION_LINES.CODE.eq(code)));
    }
}
