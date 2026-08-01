package fpt.qn.mes.user.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(10)
@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final DSLContext ctx;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final String defaultPassword;

    public AdminUserSeeder(
            DSLContext ctx,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            @Value("${app.seed.admin.password:Admin@1234}") String defaultPassword) {
        this.ctx = ctx;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> userDefs = loadJson("users.json");
        if (userDefs.isEmpty()) return;

        String encodedPassword = passwordEncoder.encode(defaultPassword);

        for (Map<String, Object> def : userDefs) {
            String username = (String) def.get("username");
            String fullName = (String) def.get("fullName");
            String roleName = (String) def.get("role");

            UUID roleId = ctx.select(ROLES.ID)
                    .from(ROLES)
                    .where(org.jooq.impl.DSL.upper(org.jooq.impl.DSL.trim(ROLES.NAME))
                            .eq(roleName.trim().toUpperCase()))
                    .fetchOne(ROLES.ID);

            if (roleId == null) {
                log.warn("Role {} not found in database; skipping user {}", roleName, username);
                continue;
            }

            UUID userId = ctx.select(USERS.ID)
                    .from(USERS)
                    .where(USERS.USERNAME.eq(username))
                    .fetchOne(USERS.ID);

            if (userId == null) {
                userId = UuidV7.generate();
                ctx.insertInto(
                                USERS,
                                USERS.ID,
                                USERS.USERNAME,
                                USERS.PASSWORD_HASH,
                                USERS.FULL_NAME,
                                USERS.ACTIVE,
                                USERS.CREATED_AT)
                        .values(
                                userId,
                                username,
                                encodedPassword,
                                fullName,
                                true,
                                OffsetDateTime.now())
                        .onConflictDoNothing()
                        .execute();
            }

            ctx.insertInto(USER_ROLES, USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
                    .values(userId, roleId)
                    .onConflictDoNothing()
                    .execute();
        }

        log.info("Seeded default users for all roles");
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
