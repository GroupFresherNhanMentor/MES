package fpt.qn.mes.auth.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.ROLES;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import fpt.qn.mes.common.util.UuidV7;
import org.jooq.DSLContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleDataSeeder implements ApplicationRunner {

    DSLContext ctx;
    ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> rows = loadJson("roles.json");
        if (rows.isEmpty()) return;

        var step = ctx.insertInto(ROLES, ROLES.ID, ROLES.NAME, ROLES.DESCRIPTION);
        for (Map<String, Object> row : rows) {
            step = step.values(UuidV7.generate(), (String) row.get("name"), (String) row.get("description"));
        }
        step.onConflictDoNothing().execute();
        log.info("Seeded roles");
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
