package fpt.qn.mes.bom.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.bom.application.dto.request.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.dto.response.BomItemDto;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.service.LookupEntry;

public interface BomUseCase {
    List<LookupEntry> getBomStatuses();
    PageResponse<BomDto> getBoms(int page, int size, UUID finishedProductId, UUID bomStatusId);
    BomDto getBomById(UUID id);
    BomDto createBom(CreateBomRequest request);
    BomDto activateBom(UUID id);
    BomDto createNewVersion(UUID id);
    BomItemDto addBomItem(UUID bomId, CreateBomItemRequest request);
    void deleteBomItem(UUID bomId, UUID itemId);
}
