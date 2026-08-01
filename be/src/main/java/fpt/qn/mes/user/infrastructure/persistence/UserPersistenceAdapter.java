package fpt.qn.mes.user.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.USERS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.exception.ConflictException;
import fpt.qn.mes.common.repository.BaseRepository;
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
        return fetchById(id).map(record -> userMapper.toDomain(record));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return ctx.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username.trim()))
                .fetchOptional()
                .map(record -> userMapper.toDomain(record));
    }

    @Override
    public User save(User user) {
        try {
            return userMapper.toDomain(create(userMapper.toRecord(user)));
        } catch (DataAccessException | DuplicateKeyException ex) {
            throw new ConflictException("Username already exists");
        }
    }

    @Override
    public User update(User user) {
        UsersRecord record = userMapper.toRecord(user);
        return userMapper.toDomain(ctx.update(USERS)
                .set(record)
                .where(USERS.ID.eq(user.getId()))
                .returning()
                .fetchOne());
    }

    @Override
    public PaginationResult<User> findAll(int page, int size) {
        long total = ctx.fetchCount(USERS);
        var records = ctx.selectFrom(USERS)
                .orderBy(USERS.USERNAME.asc(), USERS.ID.asc())
                .limit(size)
                .offset(page * size)
                .fetch();
        List<User> users = new ArrayList<>();
        for (UsersRecord record : records) {
            users.add(userMapper.toDomain(record));
        }
        return PaginationResult.<User>builder()
                .items(users)
                .total(total)
                .build();
    }

    @Override
    public boolean existsByUsername(String username) {
        return ctx.fetchExists(ctx.selectOne()
                .from(USERS)
                .where(USERS.USERNAME.eq(username.trim())));
    }
}

