package fpt.qn.mes.auth.integration;

import static fpt.qn.mes.jooq.Tables.RBAC_MUTATION_GUARD;
import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.mes.AbstractIntegrationTest;

class AuthMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    DSLContext ctx;

    @Test
    void finalAuthSchemaPreservesPermissionTablesWithoutUsingThemForAuthorization() {
        assertThat(tableExists("refresh_sessions")).isFalse();
        assertThat(tableExists("refresh_tokens")).isFalse();
        assertThat(tableExists("login_throttles")).isFalse();
        assertThat(tableExists("security_events")).isFalse();
        assertThat(ctx.fetchCount(RBAC_MUTATION_GUARD)).isEqualTo(1);

        assertThat(indexExists("uq_roles_normalized_name")).isTrue();
        assertThat(indexExists("uq_permissions_normalized_name")).isTrue();
        assertThat(deleteRule("user_roles_role_id_fkey")).isIn("RESTRICT", "NO ACTION");
        assertThat(deleteRule("role_permissions_role_id_fkey")).isIn("RESTRICT", "NO ACTION");
        assertThat(deleteRule("role_permissions_permission_id_fkey")).isIn("RESTRICT", "NO ACTION");
        assertThat(tableExists("permissions")).isTrue();
        assertThat(tableExists("role_permissions")).isTrue();
    }

    @Test
    void representativeIdentityAndRoleRowsRemainUsable() {
        var admin = ctx.select(USERS.ID, USERS.PASSWORD_HASH, USERS.ACTIVE)
                .from(USERS)
                .where(USERS.USERNAME.eq("admin"))
                .fetchOne();
        assertThat(admin).isNotNull();
        assertThat(admin.get(USERS.PASSWORD_HASH)).startsWith("$2");
        assertThat(admin.get(USERS.ACTIVE)).isTrue();
        assertThat(ctx.fetchCount(ROLES)).isGreaterThanOrEqualTo(8);
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(ctx.fetchValue(
                "select exists(select 1 from information_schema.tables "
                        + "where table_schema = 'public' and table_name = ?)",
                tableName,
                Boolean.class));
    }

    private boolean indexExists(String indexName) {
        return Boolean.TRUE.equals(ctx.fetchValue(
                "select exists(select 1 from pg_indexes "
                        + "where schemaname = 'public' and indexname = ?)",
                indexName,
                Boolean.class));
    }

    private String deleteRule(String constraintName) {
        return ctx.fetchOne(
                "select delete_rule from information_schema.referential_constraints "
                        + "where constraint_schema = 'public' and constraint_name = ?",
                constraintName)
                .get(0, String.class);
    }
}
