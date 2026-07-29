package fpt.qn.mes.auth.integration;

import static fpt.qn.mes.jooq.Tables.PERMISSIONS;
import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.ROLE_PERMISSIONS;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;
import static org.assertj.core.api.Assertions.assertThat;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.infrastructure.seed.RoleDataSeeder;

class AuthSeedIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    DSLContext ctx;

    @Autowired
    RoleDataSeeder roleDataSeeder;

    @Test
    void roleSeedIsInsertOnlyIdempotentAndLeavesLegacyPermissionDataUntouched() {
        int rolesBefore = ctx.fetchCount(ROLES);
        int permissionsBefore = ctx.fetchCount(PERMISSIONS);
        int linksBefore = ctx.fetchCount(ROLE_PERMISSIONS);
        var adminUserBefore = ctx.selectFrom(USERS)
                .where(USERS.USERNAME.eq("admin"))
                .fetchOne();
        var adminRoleBefore = ctx.selectFrom(ROLES)
                .where(ROLES.NAME.eq("ADMIN"))
                .fetchOne();
        var adminUserRoleLinksBefore = ctx.selectFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(adminUserBefore.getId()))
                .orderBy(USER_ROLES.ROLE_ID)
                .fetch()
                .intoArrays();
        var adminPermissionLinksBefore = ctx.selectFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(adminRoleBefore.getId()))
                .orderBy(ROLE_PERMISSIONS.PERMISSION_ID)
                .fetch()
                .intoArrays();
        String originalDescription = ctx.select(ROLES.DESCRIPTION)
                .from(ROLES)
                .where(ROLES.NAME.eq("ADMIN"))
                .fetchOne(ROLES.DESCRIPTION);
        ctx.update(ROLES).set(ROLES.DESCRIPTION, "Preserved description")
                .where(ROLES.NAME.eq("ADMIN")).execute();

        roleDataSeeder.run(new DefaultApplicationArguments(new String[0]));
        roleDataSeeder.run(new DefaultApplicationArguments(new String[0]));

        assertThat(ctx.fetchCount(ROLES)).isEqualTo(rolesBefore);
        assertThat(ctx.fetchCount(PERMISSIONS)).isEqualTo(permissionsBefore);
        assertThat(ctx.fetchCount(ROLE_PERMISSIONS)).isEqualTo(linksBefore);
        assertThat(ctx.selectFrom(USERS)
                .where(USERS.ID.eq(adminUserBefore.getId()))
                .fetchOne()
                .intoArray()).containsExactly(adminUserBefore.intoArray());
        assertThat(ctx.selectFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(adminUserBefore.getId()))
                .orderBy(USER_ROLES.ROLE_ID)
                .fetch()
                .intoArrays()).isDeepEqualTo(adminUserRoleLinksBefore);
        assertThat(ctx.selectFrom(ROLE_PERMISSIONS)
                .where(ROLE_PERMISSIONS.ROLE_ID.eq(adminRoleBefore.getId()))
                .orderBy(ROLE_PERMISSIONS.PERMISSION_ID)
                .fetch()
                .intoArrays()).isDeepEqualTo(adminPermissionLinksBefore);
        assertThat(ctx.select(ROLES.DESCRIPTION).from(ROLES).where(ROLES.NAME.eq("ADMIN"))
                .fetchOne(ROLES.DESCRIPTION)).isEqualTo("Preserved description");
        ctx.update(ROLES).set(ROLES.DESCRIPTION, originalDescription)
                .where(ROLES.NAME.eq("ADMIN")).execute();
    }
}
