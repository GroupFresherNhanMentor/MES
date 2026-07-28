package fpt.qn.mes.workorder.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENT_TYPES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_PRIORITIES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUS_TRANSITIONS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUSES;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

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
public class WorkOrderDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedWorkOrderStatuses();
        seedWorkOrderPriorities();
        seedWorkOrderEventTypes();
        seedWorkOrderStatusTransitions();
    }

    // work_order_statuses has is_initial and is_final columns not present on other status tables
    private void seedWorkOrderStatuses() {
        List<Map<String, Object>> rows = loadJson("work-order-statuses.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(WORK_ORDER_STATUSES,
            WORK_ORDER_STATUSES.ID,
            WORK_ORDER_STATUSES.NAME,
            WORK_ORDER_STATUSES.DESCRIPTION,
            WORK_ORDER_STATUSES.IS_INITIAL,
            WORK_ORDER_STATUSES.IS_FINAL);

        for (Map<String, Object> row : rows) {
            step = step.values(
                UUID.randomUUID(),
                (String) row.get("name"),
                (String) row.get("description"),
                (Boolean) row.get("isInitial"),
                (Boolean) row.get("isFinal"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded work_order_statuses");
    }

    private void seedWorkOrderPriorities() {
        List<Map<String, Object>> rows = loadJson("work-order-priorities.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(WORK_ORDER_PRIORITIES, WORK_ORDER_PRIORITIES.ID, WORK_ORDER_PRIORITIES.NAME, WORK_ORDER_PRIORITIES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UUID.randomUUID(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded work_order_priorities");
    }

    private void seedWorkOrderEventTypes() {
        List<Map<String, Object>> rows = loadJson("work-order-event-types.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(WORK_ORDER_EVENT_TYPES, WORK_ORDER_EVENT_TYPES.ID, WORK_ORDER_EVENT_TYPES.NAME, WORK_ORDER_EVENT_TYPES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UUID.randomUUID(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded work_order_event_types");
    }

    private void seedWorkOrderStatusTransitions() {
        Map<String, UUID> statusIdByName = new HashMap<>();
        ctx.select(WORK_ORDER_STATUSES.NAME, WORK_ORDER_STATUSES.ID)
            .from(WORK_ORDER_STATUSES)
            .fetch()
            .forEach(r -> statusIdByName.put(r.get(WORK_ORDER_STATUSES.NAME), r.get(WORK_ORDER_STATUSES.ID)));

        List<Map<String, Object>> rows = loadJson("work-order-status-transitions.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(WORK_ORDER_STATUS_TRANSITIONS,
            WORK_ORDER_STATUS_TRANSITIONS.ID,
            WORK_ORDER_STATUS_TRANSITIONS.FROM_STATUS_ID,
            WORK_ORDER_STATUS_TRANSITIONS.TO_STATUS_ID,
            WORK_ORDER_STATUS_TRANSITIONS.IS_ACTIVE,
            WORK_ORDER_STATUS_TRANSITIONS.DESCRIPTION);

        for (Map<String, Object> row : rows) {
            String fromName = (String) row.get("from");
            String toName   = (String) row.get("to");
            UUID fromId = statusIdByName.get(fromName);
            UUID toId   = statusIdByName.get(toName);
            if (fromId == null || toId == null) {
                throw new IllegalStateException(
                    "Unknown status in transition seed: " + fromName + " -> " + toName);
            }
            step = step.values(UUID.randomUUID(), fromId, toId, true, (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded work_order_status_transitions");
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
