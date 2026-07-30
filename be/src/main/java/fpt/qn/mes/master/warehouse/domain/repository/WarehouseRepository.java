package fpt.qn.mes.master.warehouse.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;

public interface WarehouseRepository {
    Optional<Warehouse> findById(UUID id);
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findByIds(Collection<UUID> ids);
    Warehouse save(Warehouse warehouse);
    Warehouse update(Warehouse warehouse);
    void deleteById(UUID id);
    PaginationResult<Warehouse> findAll(int page, int size);
    PaginationResult<Warehouse> findAllByStatus(int page, int size, UUID statusId);
    boolean existsByCode(String code);
}
