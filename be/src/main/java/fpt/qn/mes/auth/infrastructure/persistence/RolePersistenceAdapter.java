package fpt.qn.mes.auth.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.RolesRecord;
import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePersistenceAdapter extends BaseRepository<RolesRecord> implements RoleRepository {

    RoleRecordMapper mapper;

    public RolePersistenceAdapter(DSLContext ctx, RoleRecordMapper mapper) {
        super(ctx, ROLES); this.mapper = mapper;
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
        } catch (org.jooq.exception.DataAccessException
                | org.springframework.dao.DuplicateKeyException ex) {
            throw new fpt.qn.mes.common.exception.ConflictException("Role name already exists");
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
        } catch (org.jooq.exception.DataAccessException
                | org.springframework.dao.DuplicateKeyException ex) {
            throw new fpt.qn.mes.common.exception.ConflictException("Role name already exists");
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            hardDeleteById(id);
        } catch (org.jooq.exception.DataAccessException
                | org.springframework.dao.DataIntegrityViolationException ex) {
            throw new fpt.qn.mes.common.exception.ConflictException("Assigned role cannot be deleted");
        }
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(ctx.selectOne().from(ROLES)
                .where(org.jooq.impl.DSL.upper(org.jooq.impl.DSL.trim(ROLES.NAME))
                        .eq(Role.normalizeName(name))));
    }

    @Override
    public boolean isAssigned(UUID id) {
        return ctx.fetchExists(ctx.selectOne().from(USER_ROLES).where(USER_ROLES.ROLE_ID.eq(id)));
    }

}
