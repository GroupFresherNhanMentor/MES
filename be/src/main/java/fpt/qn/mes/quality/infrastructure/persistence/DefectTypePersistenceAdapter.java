package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.DEFECT_TYPES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.DefectTypesRecord;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.repository.DefectTypeRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefectTypePersistenceAdapter extends BaseRepository<DefectTypesRecord> implements DefectTypeRepository {

    DSLContext dslCtx;

    public DefectTypePersistenceAdapter(DSLContext ctx) {
        super(ctx, DEFECT_TYPES);
        this.dslCtx = ctx;
    }

    @Override
    public Optional<DefectType> findById(UUID id) {
        return dslCtx.selectFrom(DEFECT_TYPES)
            .where(DEFECT_TYPES.ID.eq(id))
            .fetchOptional(r -> DefectType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public List<DefectType> findAll() {
        return dslCtx.selectFrom(DEFECT_TYPES)
            .fetch(r -> DefectType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public DefectType save(DefectType defectType) {
        DefectTypesRecord r = new DefectTypesRecord();
        r.setId(defectType.getId() != null ? defectType.getId() : UUID.randomUUID());
        r.setName(defectType.getName());
        r.setDescription(defectType.getDescription());
        dslCtx.insertInto(DEFECT_TYPES)
            .set(r)
            .onConflict(DEFECT_TYPES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return DefectType.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .build();
    }
}
