package fpt.qn.mes.master.product.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.master.product.application.port.out.MovementStockCheckPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovementStockCheckAdapter implements MovementStockCheckPort {

    DSLContext ctx;

    @Override
    public boolean hasStockMovements(UUID productId) {
        return ctx.fetchExists(ctx.selectFrom(STOCK_MOVEMENTS)
            .where(STOCK_MOVEMENTS.PRODUCT_ID.eq(productId)));
    }

}
