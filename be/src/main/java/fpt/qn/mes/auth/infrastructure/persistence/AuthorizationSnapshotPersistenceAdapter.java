package fpt.qn.mes.auth.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.auth.application.port.out.AuthorizationSnapshotPort;
import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationSnapshotPersistenceAdapter implements AuthorizationSnapshotPort {

    DSLContext ctx;

    @Override
    public Optional<AuthorizationSnapshot> load(UUID userId) {
        Result<Record> rows = ctx.select()
                .from(USERS)
                .leftJoin(USER_ROLES).on(USER_ROLES.USER_ID.eq(USERS.ID))
                .leftJoin(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                .where(USERS.ID.eq(userId))
                .orderBy(ROLES.NAME.asc())
                .fetch();
        return assemble(rows);
    }

    private Optional<AuthorizationSnapshot> assemble(Result<Record> rows) {
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Record first = rows.getFirst();
        TreeSet<String> roles = new TreeSet<>();
        for (Record row : rows) {
            String roleName = row.get(ROLES.NAME);
            if (roleName != null) {
                roles.add(roleName);
            }
        }
        return Optional.of(AuthorizationSnapshot.builder()
                .userId(first.get(USERS.ID))
                .username(first.get(USERS.USERNAME))
                .fullName(first.get(USERS.FULL_NAME))
                .active(Boolean.TRUE.equals(first.get(USERS.ACTIVE)))
                .roles(new ArrayList<>(roles))
                .permissions(List.of())
                .build());
    }
}
