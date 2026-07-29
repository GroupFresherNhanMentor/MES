package fpt.qn.mes.master.location.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;

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
public class LocationMasterDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> rows = loadJson("location-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(LOCATION_STATUSES, LOCATION_STATUSES.ID, LOCATION_STATUSES.NAME, LOCATION_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded location_statuses");
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
