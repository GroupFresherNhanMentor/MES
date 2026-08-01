package fpt.qn.mes.master.product.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import org.jooq.DSLContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductMasterDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedProductTypes();
        seedProductStatuses();
        seedUnitsOfMeasure();
        seedProducts();
    }

    private void seedProductTypes() {
        List<Map<String, Object>> rows = loadJson("product-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(PRODUCT_TYPES, PRODUCT_TYPES.ID, PRODUCT_TYPES.NAME, PRODUCT_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded product_types");
    }

    private void seedProductStatuses() {
        List<Map<String, Object>> rows = loadJson("product-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(PRODUCT_STATUSES, PRODUCT_STATUSES.ID, PRODUCT_STATUSES.NAME, PRODUCT_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded product_statuses");
    }

    private void seedUnitsOfMeasure() {
        List<Map<String, Object>> rows = loadJson("units-of-measure.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(UNITS_OF_MEASURE, UNITS_OF_MEASURE.ID, UNITS_OF_MEASURE.NAME, UNITS_OF_MEASURE.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded units_of_measure");
    }

    private void seedProducts() {
        List<Map<String, Object>> rows = loadJson("products.json");
        if (rows.isEmpty()) return;

        for (Map<String, Object> row : rows) {
            String code = (String) row.get("code");
            String name = (String) row.get("name");
            String typeName = (String) row.get("productType");
            String unitName = (String) row.get("unit");
            String statusName = (String) row.get("productStatus");
            String version = (String) row.getOrDefault("version", "1");

            UUID typeId = ctx.select(PRODUCT_TYPES.ID)
                    .from(PRODUCT_TYPES)
                    .where(PRODUCT_TYPES.NAME.eq(typeName))
                    .fetchOne(PRODUCT_TYPES.ID);

            UUID unitId = ctx.select(UNITS_OF_MEASURE.ID)
                    .from(UNITS_OF_MEASURE)
                    .where(UNITS_OF_MEASURE.NAME.eq(unitName))
                    .fetchOne(UNITS_OF_MEASURE.ID);

            UUID statusId = ctx.select(PRODUCT_STATUSES.ID)
                    .from(PRODUCT_STATUSES)
                    .where(PRODUCT_STATUSES.NAME.eq(statusName))
                    .fetchOne(PRODUCT_STATUSES.ID);

            if (typeId != null && unitId != null && statusId != null) {
                ctx.insertInto(PRODUCTS,
                                PRODUCTS.ID,
                                PRODUCTS.CODE,
                                PRODUCTS.NAME,
                                PRODUCTS.PRODUCT_TYPE_ID,
                                PRODUCTS.UNIT_ID,
                                PRODUCTS.PRODUCT_STATUS_ID,
                                PRODUCTS.VERSION,
                                PRODUCTS.CREATED_AT,
                                PRODUCTS.UPDATED_AT)
                        .values(
                                UuidV7.generate(),
                                code,
                                name,
                                typeId,
                                unitId,
                                statusId,
                                version,
                                OffsetDateTime.now(),
                                OffsetDateTime.now())
                        .onConflictDoNothing()
                        .execute();
            }
        }
        log.info("Seeded initial products");
    }

    private List<Map<String, Object>> loadJson(String file) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("seed/" + file);
            if (is == null) throw new IllegalStateException("Seed file not found: seed/" + file);
            return objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load seed file: " + file, e);
        }
    }
}
