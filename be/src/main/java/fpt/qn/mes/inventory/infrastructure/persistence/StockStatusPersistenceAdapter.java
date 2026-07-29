package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockStatusPersistenceAdapter extends BaseRepository<StockStatusesRecord> implements StockStatusRepository {

    public StockStatusPersistenceAdapter(DSLContext ctx) {
        super(ctx, STOCK_STATUSES);
    }

    @Override
    public Optional<UUID> findIdByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return ctx.select(STOCK_STATUSES.ID)
                .from(STOCK_STATUSES)
                .where(STOCK_STATUSES.NAME.eq(name))
                .fetchOptional(STOCK_STATUSES.ID);
    }
}
