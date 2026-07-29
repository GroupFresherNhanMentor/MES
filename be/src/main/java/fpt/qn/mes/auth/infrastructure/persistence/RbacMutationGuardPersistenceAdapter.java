package fpt.qn.mes.auth.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.RBAC_MUTATION_GUARD;
import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.auth.application.port.out.RbacMutationGuardPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RbacMutationGuardPersistenceAdapter implements RbacMutationGuardPort {

    DSLContext ctx;

    @Override
    public void lock() {
        ctx.selectFrom(RBAC_MUTATION_GUARD)
                .where(RBAC_MUTATION_GUARD.ID.eq((short) 1))
                .forUpdate()
                .fetchOne();
    }

    @Override
    public boolean hasActiveAdministrator() {
        return ctx.fetchExists(ctx.select(USERS.ID)
                .from(USERS)
                .join(USER_ROLES).on(USER_ROLES.USER_ID.eq(USERS.ID))
                .join(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                .where(USERS.ACTIVE.isTrue())
                .and(org.jooq.impl.DSL.upper(org.jooq.impl.DSL.trim(ROLES.NAME)).eq("ADMIN")));
    }
}
