package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.repository.criteria.MovementTypeSearchCriteria;

public interface MovementTypeRepository {
    Optional<MovementType> findById(UUID id);
    Optional<MovementType> findByName(String name);
    Optional<UUID> findIdByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    MovementType save(MovementType movementType);
    PaginationResult<MovementType> search(MovementTypeSearchCriteria criteria);
}
