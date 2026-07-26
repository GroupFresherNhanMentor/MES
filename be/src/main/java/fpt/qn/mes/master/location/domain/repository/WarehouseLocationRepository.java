package fpt.qn.mes.master.location.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;

public interface WarehouseLocationRepository {
    Optional<WarehouseLocation> findById(UUID id);
    List<WarehouseLocation> findByWarehouseId(UUID warehouseId);
    WarehouseLocation save(WarehouseLocation location);
    WarehouseLocation update(WarehouseLocation location);
    void deleteById(UUID id);
}
