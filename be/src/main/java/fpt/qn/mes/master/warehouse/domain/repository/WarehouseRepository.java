package fpt.qn.mes.master.warehouse.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseSearchCriteria;

public interface WarehouseRepository {
    Optional<Warehouse> findById(UUID id);
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findByIds(Collection<UUID> ids);
    Warehouse save(Warehouse warehouse);
    Warehouse update(Warehouse warehouse);
    PaginationResult<Warehouse> search(WarehouseSearchCriteria criteria);
    boolean existsByCode(String code);
    boolean existsById(UUID id);

    void assignManager(UUID warehouseId, UUID userId, UUID assignedBy);
    void removeManager(UUID warehouseId, UUID userId);
    List<Warehouse.ManagerRef> findManagersByWarehouseId(UUID warehouseId);
    boolean isManagerAssigned(UUID warehouseId, UUID userId);
}
