package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.application.port.out.ProductCheckPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCheckAdapter implements ProductCheckPort {

    DSLContext ctx;

    @Override
    public boolean existsById(UUID productId) {
        return ctx.fetchExists(PRODUCTS, PRODUCTS.ID.eq(productId));
    }
}
