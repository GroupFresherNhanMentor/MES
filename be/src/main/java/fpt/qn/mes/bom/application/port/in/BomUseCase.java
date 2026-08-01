package fpt.qn.mes.bom.application.port.in;

import java.util.UUID;

import fpt.qn.mes.bom.application.dto.bom.BomResponse;
import fpt.qn.mes.bom.application.dto.bom.create.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.bom.search.BomSearchRequest;
import fpt.qn.mes.common.dto.response.PageResponse;

public interface BomUseCase {
    PageResponse<BomResponse> getBoms(BomSearchRequest request);
    BomResponse getBomById(UUID id);
    void createBom(CreateBomRequest request);
    void activateBom(UUID id);
    void deactivateBom(UUID id);
    BomResponse createNewVersion(UUID id);
}
