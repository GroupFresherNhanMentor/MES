package fpt.qn.mes.bom.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.bom.application.dto.bomitem.update.UpdateBomItemRequest;
import fpt.qn.mes.bom.application.port.in.BomItemUseCase;
import fpt.qn.mes.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/boms/{bomId}/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomItemController {

    BomItemUseCase bomItemUseCase;

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    public ResponseEntity<ApiResponse<Void>> updateBomItems(
            @PathVariable UUID bomId, @Valid @RequestBody List<UpdateBomItemRequest> items) {
        bomItemUseCase.updateBomItems(bomId, items);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }
}
