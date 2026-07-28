package fpt.qn.mes.master.product.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    }

    private void seedProductTypes() {
        List<Map<String, Object>> rows = loadJson("product-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(PRODUCT_TYPES, PRODUCT_TYPES.ID, PRODUCT_TYPES.NAME, PRODUCT_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UUID.randomUUID(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded product_types");
    }

    private void seedProductStatuses() {
        List<Map<String, Object>> rows = loadJson("product-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(PRODUCT_STATUSES, PRODUCT_STATUSES.ID, PRODUCT_STATUSES.NAME, PRODUCT_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UUID.randomUUID(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded product_statuses");
    }

    private void seedUnitsOfMeasure() {
        List<Map<String, Object>> rows = loadJson("units-of-measure.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(UNITS_OF_MEASURE, UNITS_OF_MEASURE.ID, UNITS_OF_MEASURE.NAME, UNITS_OF_MEASURE.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UUID.randomUUID(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded units_of_measure");
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
