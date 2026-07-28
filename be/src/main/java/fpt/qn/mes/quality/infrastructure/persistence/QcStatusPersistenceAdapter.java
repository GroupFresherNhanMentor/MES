package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.QC_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.QcStatusesRecord;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcStatusPersistenceAdapter extends BaseRepository<QcStatusesRecord> implements QcStatusRepository {

    DSLContext dslCtx;

    public QcStatusPersistenceAdapter(DSLContext ctx) {
        super(ctx, QC_STATUSES);
        this.dslCtx = ctx;
    }

    @Override
    public Optional<QcStatus> findById(UUID id) {
        return dslCtx.selectFrom(QC_STATUSES)
            .where(QC_STATUSES.ID.eq(id))
            .fetchOptional(r -> QcStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public List<QcStatus> findAll() {
        return dslCtx.selectFrom(QC_STATUSES)
            .fetch(r -> QcStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public QcStatus save(QcStatus status) {
        QcStatusesRecord r = new QcStatusesRecord();
        r.setId(status.getId() != null ? status.getId() : UUID.randomUUID());
        r.setName(status.getName());
        r.setDescription(status.getDescription());
        dslCtx.insertInto(QC_STATUSES)
            .set(r)
            .onConflict(QC_STATUSES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return QcStatus.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .build();
    }
}
