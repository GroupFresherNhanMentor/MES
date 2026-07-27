package fpt.qn.mes.master.warehouse.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;

public interface WarehouseRepository {
    Optional<Warehouse> findById(UUID id);
    Warehouse save(Warehouse warehouse);
    Warehouse update(Warehouse warehouse);
    void deleteById(UUID id);
    PaginationResult<Warehouse> findAll(int page, int size);
    boolean existsByCode(String code);
}
