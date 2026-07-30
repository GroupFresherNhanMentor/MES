package fpt.qn.mes.master.location.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.repository.criteria.WarehouseLocationSearchCriteria;

public interface WarehouseLocationRepository {
    Optional<WarehouseLocation> findById(UUID id);
    List<WarehouseLocation> findByIds(java.util.Collection<UUID> ids);
    WarehouseLocation save(WarehouseLocation location);
    WarehouseLocation update(WarehouseLocation location);
    PaginationResult<WarehouseLocation> search(WarehouseLocationSearchCriteria criteria);
    boolean existsByWarehouseIdAndCode(UUID warehouseId, String code);
    boolean existsById(UUID id);
}
