package fpt.qn.mes.bom.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.bom.application.dto.BomDto;
import fpt.qn.mes.bom.application.dto.BomItemDto;
import fpt.qn.mes.bom.application.dto.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.CreateBomRequest;
import fpt.qn.mes.bom.application.port.in.BomUseCase;
import fpt.qn.mes.common.dto.ApiResponse;
import fpt.qn.mes.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomController {

    BomUseCase bomUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BomDto>>> getBoms(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BomDto>> getBomById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BomDto>> createBom(
            @Valid @RequestBody CreateBomRequest request, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBom(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{bomId}/items")
    public ResponseEntity<ApiResponse<BomItemDto>> addBomItem(
            @PathVariable UUID bomId, @Valid @RequestBody CreateBomItemRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{bomId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteBomItem(
            @PathVariable UUID bomId, @PathVariable UUID itemId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBomStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
