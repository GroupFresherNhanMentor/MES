package fpt.qn.mes.inventory.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.movementtype.MovementTypeResponse;
import fpt.qn.mes.inventory.application.dto.movementtype.create.CreateMovementTypeRequest;
import fpt.qn.mes.inventory.application.dto.movementtype.search.MovementTypeSearchRequest;

public interface MovementTypeUseCase {
    PageResponse<MovementTypeResponse> getMovementTypes(MovementTypeSearchRequest request);
    void createMovementType(CreateMovementTypeRequest request);
    UUID getMovementTypeIdByName(String name);
}
