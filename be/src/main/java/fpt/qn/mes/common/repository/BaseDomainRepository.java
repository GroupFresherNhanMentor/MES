package fpt.qn.mes.common.repository;

import java.util.Optional;

public interface BaseDomainRepository<T, ID> {
    Optional<T> findById(ID id);
}
