package fpt.qn.mes.master.line.domain.repository;

import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.line.domain.entities.Line;
import fpt.qn.mes.master.line.domain.repository.criteria.LineSearchCriteria;

public interface LineRepository {
    Optional<Line> findById(UUID id);
    Line save(Line line);
    Line update(Line line);
    PaginationResult<Line> search(LineSearchCriteria criteria);
    boolean existsByCode(String code);
    boolean existsById(UUID id);
}
