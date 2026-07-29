package fpt.qn.mes.inventory.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.LOT_TYPES;
import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;

import java.io.InputStream;
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
public class InventoryDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedLotTypes();
        seedStockStatuses();
        seedMovementTypes();
    }

    private void seedLotTypes() {
        List<Map<String, Object>> rows = loadJson("lot-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(LOT_TYPES, LOT_TYPES.ID, LOT_TYPES.NAME, LOT_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded lot_types");
    }

    private void seedStockStatuses() {
        List<Map<String, Object>> rows = loadJson("stock-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(STOCK_STATUSES, STOCK_STATUSES.ID, STOCK_STATUSES.NAME, STOCK_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded stock_statuses");
    }

    private void seedMovementTypes() {
        List<Map<String, Object>> rows = loadJson("movement-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(MOVEMENT_TYPES, MOVEMENT_TYPES.ID, MOVEMENT_TYPES.NAME, MOVEMENT_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded movement_types");
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
