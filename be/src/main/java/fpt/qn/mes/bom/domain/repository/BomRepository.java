package fpt.qn.mes.bom.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.common.dto.response.PaginationResult;

public interface BomRepository {
    Optional<Bom> findById(UUID id);
    Bom save(Bom bom);
    PaginationResult<Bom> findAll(int page, int size);
    boolean existsByFinishedProductIdAndVersion(UUID finishedProductId, Integer version);
    Optional<UUID> findStatusIdByName(String name);
    BomItem saveItem(BomItem item);
    Optional<BomItem> findItemById(UUID itemId);
    void deleteItemById(UUID itemId);
}
