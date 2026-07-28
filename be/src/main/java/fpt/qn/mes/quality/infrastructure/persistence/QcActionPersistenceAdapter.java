package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.QC_ACTIONS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.QcActionsRecord;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcActionPersistenceAdapter extends BaseRepository<QcActionsRecord> implements QcActionRepository {

    DSLContext dslCtx;

    public QcActionPersistenceAdapter(DSLContext ctx) {
        super(ctx, QC_ACTIONS);
        this.dslCtx = ctx;
    }

    @Override
    public Optional<QcAction> findById(UUID id) {
        return dslCtx.selectFrom(QC_ACTIONS)
            .where(QC_ACTIONS.ID.eq(id))
            .fetchOptional(r -> QcAction.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public List<QcAction> findAll() {
        return dslCtx.selectFrom(QC_ACTIONS)
            .fetch(r -> QcAction.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public QcAction save(QcAction action) {
        QcActionsRecord r = new QcActionsRecord();
        r.setId(action.getId() != null ? action.getId() : UUID.randomUUID());
        r.setName(action.getName());
        r.setDescription(action.getDescription());
        dslCtx.insertInto(QC_ACTIONS)
            .set(r)
            .onConflict(QC_ACTIONS.ID)
            .doUpdate()
            .set(r)
            .execute();
        return QcAction.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .build();
    }
}
