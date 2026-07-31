package fpt.qn.mes.master.line.domain.repository;

import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.line.domain.entities.LineStatus;
import fpt.qn.mes.master.line.domain.repository.criteria.LineStatusSearchCriteria;

public interface LineStatusRepository {
    Optional<LineStatus> findById(UUID id);
    Optional<LineStatus> findByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    LineStatus save(LineStatus status);
    PaginationResult<LineStatus> search(LineStatusSearchCriteria criteria);
}
