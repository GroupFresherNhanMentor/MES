package fpt.qn.mes.auth.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.RoleRepository;
import fpt.qn.mes.auth.application.exception.RoleConflictException;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.RolesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePersistenceAdapter extends BaseRepository<RolesRecord> implements RoleRepository {

    RoleRecordMapper mapper;

    public RolePersistenceAdapter(DSLContext ctx, RoleRecordMapper mapper) {
        super(ctx, ROLES);
        this.mapper = mapper;
    }

    @Override
    public List<Role> findAll() {
        return ctx.selectFrom(ROLES)
                .orderBy(ROLES.NAME.asc(), ROLES.ID.asc())
                .fetch(record -> mapper.toDomain(record));
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return ctx.selectFrom(ROLES)
                .where(ROLES.ID.eq(id))
                .fetchOptional(record -> mapper.toDomain(record));
    }

    @Override
    public Role save(Role role) {
        try {
            return mapper.toDomain(create(mapper.toRecord(role)));
        } catch (DataAccessException | DuplicateKeyException ex) {
            throw new RoleConflictException("Role name already exists");
        }
    }

    @Override
    public Role update(Role role) {
        try {
            var stored = ctx.update(ROLES)
                    .set(mapper.toRecord(role))
                    .where(ROLES.ID.eq(role.getId()))
                    .returning()
                    .fetchOne();
            return mapper.toDomain(stored);
        } catch (DataAccessException | DuplicateKeyException ex) {
            throw new RoleConflictException("Role name already exists");
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            hardDeleteById(id);
        } catch (DataAccessException | DataIntegrityViolationException ex) {
            throw new RoleConflictException("Assigned role cannot be deleted");
        }
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(ctx.selectOne().from(ROLES)
                .where(DSL.upper(DSL.trim(ROLES.NAME))
                        .eq(Role.normalizeName(name))));
    }

    @Override
    public boolean isAssigned(UUID id) {
        return ctx.fetchExists(ctx.selectOne().from(USER_ROLES).where(USER_ROLES.ROLE_ID.eq(id)));
    }
}
