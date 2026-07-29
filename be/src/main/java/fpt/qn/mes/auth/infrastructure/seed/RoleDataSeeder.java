package fpt.qn.mes.auth.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.ROLES;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

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
        seedRoles();
        log.info("Reconciled roles");
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

    private void seedRoles() {
        for (Map<String, Object> row : loadJson("roles.json")) {
            String name = (String) row.get("name");
            ctx.insertInto(ROLES, ROLES.ID, ROLES.NAME, ROLES.DESCRIPTION)
                    .values(stableId("role", name), name, (String) row.get("description"))
                    .onConflictDoNothing()
                    .execute();
        }
    }

    private UUID stableId(String type, String name) {
        return UUID.nameUUIDFromBytes((type + ":" + name).getBytes(StandardCharsets.UTF_8));
    }
}
