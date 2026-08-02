package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.repository.criteria.LotTypeSearchCriteria;

public interface LotTypeRepository {
    Optional<LotType> findById(UUID id);
    Optional<LotType> findByName(String name);
    Optional<UUID> findIdByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    LotType save(LotType lotType);
    PaginationResult<LotType> search(LotTypeSearchCriteria criteria);
}
