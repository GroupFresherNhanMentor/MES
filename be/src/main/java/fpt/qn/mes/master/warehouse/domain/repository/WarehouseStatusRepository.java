package fpt.qn.mes.master.warehouse.domain.repository;

import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseStatusSearchCriteria;

public interface WarehouseStatusRepository {
    Optional<WarehouseStatus> findById(UUID id);
    Optional<WarehouseStatus> findByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    WarehouseStatus save(WarehouseStatus status);
    PaginationResult<WarehouseStatus> search(WarehouseStatusSearchCriteria criteria);
}
