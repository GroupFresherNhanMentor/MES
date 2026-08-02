package fpt.qn.mes.auth.support;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;
import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthTestData {

    DSLContext ctx;
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Identity createIdentity(
            String username,
            String rawPassword,
            boolean active,
            List<String> roleNames) {
        UUID userId = UUID.randomUUID();
        ctx.insertInto(USERS)
                .set(USERS.ID, userId)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD_HASH, passwordEncoder.encode(rawPassword))
                .set(USERS.FULL_NAME, "Test " + username)
                .set(USERS.ACTIVE, active)
                .set(USERS.CREATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .execute();

        List<UUID> roleIds = roleNames.stream()
                .map(roleName -> ensureRole(roleName))
                .toList();
        roleIds.forEach(roleId -> {
            ctx.insertInto(USER_ROLES)
                    .set(USER_ROLES.USER_ID, userId)
                    .set(USER_ROLES.ROLE_ID, roleId)
                    .onConflictDoNothing()
                    .execute();
        });

        return Identity.builder()
                .userId(userId)
                .username(username)
                .rawPassword(rawPassword)
                .roleIds(roleIds)
                .build();
    }

    public void deleteIdentity(Identity identity) {
        if (identity == null || identity.getUserId() == null) return;
        ctx.deleteFrom(AUDIT_LOGS)
                .where(AUDIT_LOGS.ACTOR_ID.eq(identity.getUserId()))
                .execute();
        ctx.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_ID.eq(identity.getUserId()))
                .execute();
        ctx.deleteFrom(USERS)
                .where(USERS.ID.eq(identity.getUserId()))
                .execute();
        for (UUID roleId : identity.getRoleIds()) {
            boolean testRole = ctx.fetchExists(ctx.selectOne().from(ROLES)
                    .where(ROLES.ID.eq(roleId))
                    .and(ROLES.DESCRIPTION.like("Test role %")));
            boolean assigned = ctx.fetchExists(ctx.selectOne().from(USER_ROLES)
                    .where(USER_ROLES.ROLE_ID.eq(roleId)));
            if (testRole && !assigned) {
                ctx.deleteFrom(ROLES).where(ROLES.ID.eq(roleId)).execute();
            }
        }
    }

    UUID ensureRole(String name) {
        UUID existingId = ctx.select(ROLES.ID)
                .from(ROLES)
                .where(ROLES.NAME.eq(name))
                .fetchOne(ROLES.ID);
        if (existingId != null) {
            return existingId;
        }
        UUID id = UUID.randomUUID();
        ctx.insertInto(ROLES)
                .set(ROLES.ID, id)
                .set(ROLES.NAME, name)
                .set(ROLES.DESCRIPTION, "Test role " + name)
                .execute();
        return id;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Identity {
        UUID userId;
        String username;
        String rawPassword;
        List<UUID> roleIds;
    }
}
