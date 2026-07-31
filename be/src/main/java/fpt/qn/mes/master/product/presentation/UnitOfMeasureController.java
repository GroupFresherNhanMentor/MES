package fpt.qn.mes.master.product.presentation;

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
import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.create.CreateUnitOfMeasureRequest;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.search.UnitOfMeasureSearchRequest;
import fpt.qn.mes.master.product.application.port.in.UnitOfMeasureUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/units-of-measure")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnitOfMeasureController {

    UnitOfMeasureUseCase unitOfMeasureUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UnitOfMeasureResponse>>> getUnitsOfMeasure(
            @ModelAttribute UnitOfMeasureSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(unitOfMeasureUseCase.getUnitsOfMeasure(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUnitOfMeasure(
            @Valid @RequestBody CreateUnitOfMeasureRequest request) {
        unitOfMeasureUseCase.createUnitOfMeasure(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
