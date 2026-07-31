package fpt.qn.mes.inventory.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.lottype.LotTypeResponse;
import fpt.qn.mes.inventory.application.dto.lottype.create.CreateLotTypeRequest;
import fpt.qn.mes.inventory.application.dto.lottype.search.LotTypeSearchRequest;
import fpt.qn.mes.inventory.application.port.in.LotTypeUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/lot-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LotTypeController {

    LotTypeUseCase lotTypeUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LotTypeResponse>>> getLotTypes(
            @ModelAttribute LotTypeSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lotTypeUseCase.getLotTypes(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createLotType(
            @Valid @RequestBody CreateLotTypeRequest request) {
        lotTypeUseCase.createLotType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
