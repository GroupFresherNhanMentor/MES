package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.inventory.domain.repository.criteria.StockStatusSearchCriteria;

public interface StockStatusRepository {
    Optional<StockStatus> findById(UUID id);
    Optional<StockStatus> findByName(String name);
    Optional<UUID> findIdByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    StockStatus save(StockStatus stockStatus);
    PaginationResult<StockStatus> search(StockStatusSearchCriteria criteria);
}
