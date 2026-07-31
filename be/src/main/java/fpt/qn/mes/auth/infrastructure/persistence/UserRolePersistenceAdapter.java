package fpt.qn.mes.auth.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.UserRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRolePersistenceAdapter implements UserRoleRepository {

    DSLContext ctx;
    RoleRecordMapper mapper;

    @Override
    public List<Role> findRolesByUserId(UUID userId) {
        return ctx.select(ROLES.asterisk())
                .from(USER_ROLES)
                .join(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                .where(USER_ROLES.USER_ID.eq(userId))
                .orderBy(ROLES.NAME.asc(), ROLES.ID.asc())
                .fetch(row -> mapper.toDomain(row.into(ROLES)));
    }

    @Override
    public long countExistingRoleIds(List<UUID> roleIds) {
        if (roleIds.isEmpty()) {
            return 0;
        }
        return ctx.selectCount().from(ROLES).where(ROLES.ID.in(roleIds)).fetchOne(0, long.class);
    }

    @Override
    public void replaceRoles(UUID userId, List<UUID> roleIds) {
        ctx.deleteFrom(USER_ROLES).where(USER_ROLES.USER_ID.eq(userId)).execute();
        if (!roleIds.isEmpty()) {
            var insert = ctx.insertInto(
                    USER_ROLES,
                    USER_ROLES.USER_ID,
                    USER_ROLES.ROLE_ID);
            for (UUID roleId : roleIds) {
                insert.values(userId, roleId);
            }
            insert.execute();
        }
    }
}
