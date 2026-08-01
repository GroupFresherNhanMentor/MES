package fpt.qn.mes.bom.application.port.in;

import java.util.List;

import fpt.qn.mes.bom.application.dto.bomstatus.BomStatusResponse;
import fpt.qn.mes.bom.application.dto.bomstatus.create.CreateBomStatusRequest;

public interface BomStatusUseCase {
    List<BomStatusResponse> getBomStatuses();
    void createBomStatus(CreateBomStatusRequest request);
}
