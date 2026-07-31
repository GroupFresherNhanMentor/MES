package fpt.qn.mes.quality.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.DEFECT_TYPES;
import static fpt.qn.mes.jooq.Tables.QC_ACTIONS;
import static fpt.qn.mes.jooq.Tables.QC_STATUSES;

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
public class QualityDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedQcStatuses();
        seedQcActions();
        seedDefectTypes();
    }

    private void seedQcStatuses() {
        List<Map<String, Object>> rows = loadJson("qc-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(QC_STATUSES, QC_STATUSES.ID, QC_STATUSES.NAME, QC_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded qc_statuses");
    }

    private void seedQcActions() {
        List<Map<String, Object>> rows = loadJson("qc-actions.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(QC_ACTIONS, QC_ACTIONS.ID, QC_ACTIONS.NAME, QC_ACTIONS.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded qc_actions");
    }

    private void seedDefectTypes() {
        List<Map<String, Object>> rows = loadJson("defect-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(DEFECT_TYPES, DEFECT_TYPES.ID, DEFECT_TYPES.NAME, DEFECT_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded defect_types");
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
