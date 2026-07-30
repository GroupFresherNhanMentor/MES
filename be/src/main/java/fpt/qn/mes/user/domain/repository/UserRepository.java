package fpt.qn.mes.user.domain.repository;

import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.user.domain.entities.User;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    User save(User user);

    User update(User user);

    PaginationResult<User> findAll(int page, int size);

    boolean existsByUsername(String username);
}
