package fpt.qn.mes.auth.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.USERS;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.application.security.CredentialAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CredentialPersistenceAdapter implements CredentialQueryPort {

    DSLContext ctx;

    @Override
    public Optional<CredentialAccount> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return ctx.select(USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.FULL_NAME, USERS.ACTIVE)
                .from(USERS)
                .where(USERS.USERNAME.eq(username.trim()))
                .fetchOptional()
                .map(row -> toAccount(
                        row.get(USERS.ID),
                        row.get(USERS.USERNAME),
                        row.get(USERS.PASSWORD_HASH),
                        row.get(USERS.FULL_NAME),
                        row.get(USERS.ACTIVE)));
    }

    @Override
    public Optional<CredentialAccount> findById(UUID id) {
        return ctx.select(USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.FULL_NAME, USERS.ACTIVE)
                .from(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional()
                .map(row -> toAccount(
                        row.get(USERS.ID),
                        row.get(USERS.USERNAME),
                        row.get(USERS.PASSWORD_HASH),
                        row.get(USERS.FULL_NAME),
                        row.get(USERS.ACTIVE)));
    }

    private CredentialAccount toAccount(
            UUID id,
            String username,
            String passwordHash,
            String fullName,
            Boolean active) {
        return CredentialAccount.builder()
                .id(id)
                .username(username)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .active(Boolean.TRUE.equals(active))
                .build();
    }
}
