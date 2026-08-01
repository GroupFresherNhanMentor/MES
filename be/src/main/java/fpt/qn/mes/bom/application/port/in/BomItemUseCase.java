package fpt.qn.mes.bom.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.bom.application.dto.bomitem.update.UpdateBomItemRequest;

public interface BomItemUseCase {
    void updateBomItems(UUID bomId, List<UpdateBomItemRequest> items);
}
