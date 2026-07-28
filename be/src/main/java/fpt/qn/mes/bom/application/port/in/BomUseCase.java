package fpt.qn.mes.bom.application.port.in;

import java.util.UUID;

import fpt.qn.mes.bom.application.dto.request.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.dto.response.BomItemDto;
import fpt.qn.mes.common.dto.response.PageResponse;

public interface BomUseCase {
    PageResponse<BomDto> getBoms(int page, int size);
    BomDto getBomById(UUID id);
    BomDto createBom(CreateBomRequest request);
    BomDto activateBom(UUID id);
    BomItemDto addBomItem(UUID bomId, CreateBomItemRequest request);
    void deleteBomItem(UUID bomId, UUID itemId);
}
