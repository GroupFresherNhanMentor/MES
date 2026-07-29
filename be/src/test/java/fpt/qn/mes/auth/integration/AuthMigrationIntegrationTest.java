package fpt.qn.mes.auth.integration;

import static fpt.qn.mes.jooq.Tables.PERMISSIONS;
import static fpt.qn.mes.jooq.Tables.RBAC_MUTATION_GUARD;
import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.mes.AbstractIntegrationTest;

class AuthMigrationIntegrationTest extends AbstractIntegrationTest {

    static final String MIGRATION_SHA256 =
            "95c17a0fb564e8c3f04146f6e5bab0e2da28e0c7e25116b2fd03b53836129a67";

    @Autowired
    DSLContext ctx;

    @Test
    void forwardOnlyMigrationHasStableContentAndExpectedSchemaSafety() throws Exception {
        Path migration = Path.of(
                "src",
                "main",
                "resources",
                "db",
                "migration",
                "V20260728150000__complete_auth_rbac.sql");
        String normalizedMigration = Files.readString(migration).replace("\r\n", "\n");
        String digest = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(normalizedMigration.getBytes(StandardCharsets.UTF_8)));
        assertThat(digest).isEqualTo(MIGRATION_SHA256);
        String migrationSql = Files.readString(migration).toUpperCase(java.util.Locale.ROOT);
        assertThat(migrationSql)
                .contains("CANNOT NORMALIZE ROLES")
                .contains("CANNOT NORMALIZE PERMISSIONS")
                .doesNotContain(
                        "UPDATE USERS",
                        "UPDATE ROLES",
                        "UPDATE PERMISSIONS",
                        "DELETE FROM USERS",
                        "DELETE FROM ROLES",
                        "DELETE FROM PERMISSIONS");

        var history = ctx.fetchOne(
                "select checksum, success from flyway_schema_history where version = ?",
                "20260728150000");
        assertThat(history).isNotNull();
        assertThat(history.get("checksum")).isNotNull();
        assertThat(history.get("success", Boolean.class)).isTrue();

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
    }

    @Test
    void representativeLegacyIdentityAndRbacRowsRemainUsable() {
        var admin = ctx.select(USERS.ID, USERS.PASSWORD_HASH, USERS.ACTIVE)
                .from(USERS)
                .where(USERS.USERNAME.eq("admin"))
                .fetchOne();
        assertThat(admin).isNotNull();
        assertThat(admin.get(USERS.PASSWORD_HASH)).startsWith("$2");
        assertThat(admin.get(USERS.ACTIVE)).isTrue();
        assertThat(ctx.fetchCount(ROLES)).isGreaterThanOrEqualTo(8);
        assertThat(tableExists("permissions")).isTrue();
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
