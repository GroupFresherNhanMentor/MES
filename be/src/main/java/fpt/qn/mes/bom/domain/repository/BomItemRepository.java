package fpt.qn.mes.bom.domain.repository;

import java.util.UUID;

import fpt.qn.mes.bom.domain.entities.BomItem;

public interface BomItemRepository {
    BomItem save(BomItem item);
    void deleteByBomId(UUID bomId);
}
