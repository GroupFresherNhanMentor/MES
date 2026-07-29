package fpt.qn.mes.user.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(10)
@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final DSLContext ctx;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public AdminUserSeeder(
            DSLContext ctx,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin.password:Admin@1234}") String adminPassword) {
        this.ctx = ctx;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        UUID adminRoleId = ctx.select(ROLES.ID)
                .from(ROLES)
                .where(org.jooq.impl.DSL.upper(org.jooq.impl.DSL.trim(ROLES.NAME))
                        .eq("ADMIN"))
                .fetchOne(ROLES.ID);
        if (adminRoleId == null) {
            throw new IllegalStateException("ADMIN role not found; RoleDataSeeder must run first");
        }

        UUID userId = ctx.select(USERS.ID)
                .from(USERS)
                .where(USERS.USERNAME.eq("admin"))
                .fetchOne(USERS.ID);
        if (userId == null) {
            userId = UUID.nameUUIDFromBytes("user:admin".getBytes(StandardCharsets.UTF_8));
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
                            "admin",
                            passwordEncoder.encode(adminPassword),
                            "System Administrator",
                            true,
                            OffsetDateTime.now())
                    .onConflictDoNothing()
                    .execute();
            log.warn("Admin user seeded; override app.seed.admin.password before production");
        }

        ctx.insertInto(USER_ROLES, USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
                .values(userId, adminRoleId)
                .onConflictDoNothing()
                .execute();
        log.debug("Administrator role link reconciled");
    }
}
