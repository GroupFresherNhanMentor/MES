package fpt.qn.mes.quality.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.repository.criteria.DefectTypeSearchCriteria;

public interface DefectTypeRepository {
    Optional<DefectType> findById(UUID id);
    DefectType save(DefectType defectType);
    PaginationResult<DefectType> search(DefectTypeSearchCriteria criteria);
}
