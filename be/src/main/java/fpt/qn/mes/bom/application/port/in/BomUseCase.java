package fpt.qn.mes.bom.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.bom.application.dto.request.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomResponse;
import fpt.qn.mes.bom.application.dto.response.BomItemResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.service.LookupEntry;

public interface BomUseCase {
    List<LookupEntry> getBomStatuses();
    PageResponse<BomResponse> getBoms(int page, int size, UUID finishedProductId, UUID bomStatusId);
    BomResponse getBomById(UUID id);
    BomResponse createBom(CreateBomRequest request);
    BomResponse activateBom(UUID id);
    BomResponse createNewVersion(UUID id);
    BomItemResponse addBomItem(UUID bomId, CreateBomItemRequest request);
    void deleteBomItem(UUID bomId, UUID itemId);
}
