package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovementTypePersistenceAdapter extends BaseRepository<MovementTypesRecord> implements MovementTypeRepository {

    public MovementTypePersistenceAdapter(DSLContext ctx) {
        super(ctx, MOVEMENT_TYPES);
    }

    @Override
    public Optional<MovementType> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return fetchById(id).map(r -> MovementType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public Optional<UUID> findIdByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return ctx.select(MOVEMENT_TYPES.ID)
                .from(MOVEMENT_TYPES)
                .where(MOVEMENT_TYPES.NAME.eq(name))
                .fetchOptional(MOVEMENT_TYPES.ID);
    }
}
