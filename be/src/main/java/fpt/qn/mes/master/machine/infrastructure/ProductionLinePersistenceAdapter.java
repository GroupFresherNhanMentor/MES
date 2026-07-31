package fpt.qn.mes.master.machine.infrastructure;

import static fpt.qn.mes.jooq.Tables.LINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_LINES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.master.machine.application.port.out.ProductionLinePort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionLinePersistenceAdapter implements ProductionLinePort {

    DSLContext ctx;

    @Override
    public Optional<Boolean> isProductionLineActive(UUID productionLineId) {
        return ctx.select(LINE_STATUSES.NAME.eq("ACTIVE"))
                .from(PRODUCTION_LINES)
                .join(LINE_STATUSES).on(PRODUCTION_LINES.LINE_STATUS_ID.eq(LINE_STATUSES.ID))
                .where(PRODUCTION_LINES.ID.eq(productionLineId))
                .fetchOptionalInto(Boolean.class);
    }
}
