package fpt.qn.mes.maintenance.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.MAINTENANCE_TICKET_PRIORITIES;
import static fpt.qn.mes.jooq.Tables.MAINTENANCE_TICKET_STATUSES;
import static fpt.qn.mes.jooq.Tables.MAINTENANCE_TICKET_TYPES;

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
public class MaintenanceDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedTicketTypes();
        seedTicketStatuses();
        seedTicketPriorities();
    }

    private void seedTicketTypes() {
        List<Map<String, Object>> rows = loadJson("maintenance-ticket-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(MAINTENANCE_TICKET_TYPES, MAINTENANCE_TICKET_TYPES.ID, MAINTENANCE_TICKET_TYPES.NAME, MAINTENANCE_TICKET_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded maintenance_ticket_types");
    }

    private void seedTicketStatuses() {
        List<Map<String, Object>> rows = loadJson("maintenance-ticket-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(MAINTENANCE_TICKET_STATUSES, MAINTENANCE_TICKET_STATUSES.ID, MAINTENANCE_TICKET_STATUSES.NAME, MAINTENANCE_TICKET_STATUSES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded maintenance_ticket_statuses");
    }

    private void seedTicketPriorities() {
        List<Map<String, Object>> rows = loadJson("maintenance-ticket-priorities.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(MAINTENANCE_TICKET_PRIORITIES, MAINTENANCE_TICKET_PRIORITIES.ID, MAINTENANCE_TICKET_PRIORITIES.NAME, MAINTENANCE_TICKET_PRIORITIES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded maintenance_ticket_priorities");
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
