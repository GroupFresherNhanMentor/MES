package fpt.qn.mes.master.warehouse.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.WAREHOUSE_STATUSES;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
public class WarehouseMasterDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> rows = loadJson("warehouse-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(WAREHOUSE_STATUSES, WAREHOUSE_STATUSES.ID, WAREHOUSE_STATUSES.NAME, WAREHOUSE_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded warehouse_statuses");
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
