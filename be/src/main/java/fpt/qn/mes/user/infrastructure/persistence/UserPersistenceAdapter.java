package fpt.qn.mes.user.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.USERS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.user.domain.entities.User;
import fpt.qn.mes.user.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPersistenceAdapter extends BaseRepository<UsersRecord> implements UserRepository {

    UserRecordMapper userMapper;

    public UserPersistenceAdapter(DSLContext ctx, UserRecordMapper userMapper) {
        super(ctx, USERS);
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        UsersRecord record = ctx.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(userMapper.toDomain(record));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UsersRecord record = ctx.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOne();
        return Optional.ofNullable(userMapper.toDomain(record));
    }

    @Override
    public User save(User user) {
        UsersRecord record = userMapper.toRecord(user);
        if (record.getId() == null) {
            record.setId(UuidV7.generate());
        }
        ctx.attach(record);
        record.store();
        return userMapper.toDomain(record);
    }

    @Override
    public User update(User user) {
        UsersRecord record = userMapper.toRecord(user);
        ctx.attach(record);
        record.update();
        return userMapper.toDomain(record);
    }

    @Override
    public PaginationResult<User> findAll(int page, int size) {
        long totalElements = count();
        List<User> items = ctx.selectFrom(USERS)
                .orderBy(USERS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(userMapper::toDomain);

        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PaginationResult.<User>builder()
                .content(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public boolean existsByUsername(String username) {
        return ctx.fetchExists(ctx.selectFrom(USERS).where(USERS.USERNAME.eq(username)));
    }
}
