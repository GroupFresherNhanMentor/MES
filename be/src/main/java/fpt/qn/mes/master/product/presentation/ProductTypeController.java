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
import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.application.dto.producttype.create.CreateProductTypeRequest;
import fpt.qn.mes.master.product.application.dto.producttype.search.ProductTypeSearchRequest;
import fpt.qn.mes.master.product.application.port.in.ProductTypeUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/product-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypeController {

    ProductTypeUseCase productTypeUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductTypeResponse>>> getProductTypes(
            @ModelAttribute ProductTypeSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productTypeUseCase.getProductTypes(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createProductType(
            @Valid @RequestBody CreateProductTypeRequest request) {
        productTypeUseCase.createProductType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
