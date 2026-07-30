package fpt.qn.mes.master.product.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;
import fpt.qn.mes.master.product.domain.repository.criteria.UnitOfMeasureSearchCriteria;

public interface UnitOfMeasureRepository {
    Optional<UnitOfMeasure> findById(UUID id);
    List<UnitOfMeasure> findAll();
    UnitOfMeasure save(UnitOfMeasure unit);
    PaginationResult<UnitOfMeasure> search(UnitOfMeasureSearchCriteria criteria);
}
