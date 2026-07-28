package fpt.qn.mes.user.infrastructure.seed;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(10)
@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final DSLContext ctx;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public AdminUserSeeder(DSLContext ctx,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seed.admin.password:Admin@1234}") String adminPassword) {
        this.ctx = ctx;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (ctx.fetchExists(USERS, USERS.USERNAME.eq("admin"))) {
            log.debug("Admin user already exists, skipping");
            return;
        }

        UUID adminRoleId = ctx.select(ROLES.ID)
            .from(ROLES)
            .where(ROLES.NAME.eq("ADMIN"))
            .fetchOneInto(UUID.class);

        if (adminRoleId == null) {
            throw new IllegalStateException("ADMIN role not found — RoleDataSeeder must run before AdminUserSeeder");
        }

        UUID userId = UUID.randomUUID();

        ctx.insertInto(USERS, USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.FULL_NAME, USERS.ACTIVE, USERS.CREATED_AT)
            .values(userId, "admin", passwordEncoder.encode(adminPassword), "System Administrator", true, OffsetDateTime.now())
            .onConflictDoNothing()
            .execute();

        ctx.insertInto(USER_ROLES, USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
            .values(userId, adminRoleId)
            .onConflictDoNothing()
            .execute();

        log.warn("Admin user seeded with default password — override via app.seed.admin.password property before going to production");
    }
}
