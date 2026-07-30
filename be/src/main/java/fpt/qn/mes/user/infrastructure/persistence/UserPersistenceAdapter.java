package fpt.qn.mes.user.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.USERS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
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
        super(ctx, USERS); this.userMapper = userMapper;
    }

    @Override public Optional<User> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Optional<User> findByUsername(String username) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public User save(User user) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public User update(User user) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<User> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public boolean existsByUsername(String username) { throw new UnsupportedOperationException("Not implemented"); }
}
