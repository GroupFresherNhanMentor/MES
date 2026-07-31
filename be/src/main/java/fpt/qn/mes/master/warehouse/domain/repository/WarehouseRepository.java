package fpt.qn.mes.master.warehouse.domain.repository;

import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseSearchCriteria;

public interface WarehouseRepository {
    Optional<Warehouse> findById(UUID id);
    Warehouse save(Warehouse warehouse);
    Warehouse update(Warehouse warehouse);
    PaginationResult<Warehouse> search(WarehouseSearchCriteria criteria);
    boolean existsByCode(String code);
    boolean existsById(UUID id);
}
