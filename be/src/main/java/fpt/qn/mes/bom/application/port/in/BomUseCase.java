package fpt.qn.mes.bom.application.port.in;

import java.util.UUID;

import fpt.qn.mes.bom.application.dto.*;
import fpt.qn.mes.common.dto.PageResponse;

public interface BomUseCase {
    PageResponse<BomDto> getBoms(int page, int size);
    BomDto getBomById(UUID id);
    BomDto createBom(CreateBomRequest request, UUID currentUserId);
    void deleteBom(UUID id);
    BomItemDto addBomItem(UUID bomId, CreateBomItemRequest request);
    void deleteBomItem(UUID bomId, UUID itemId);
}
