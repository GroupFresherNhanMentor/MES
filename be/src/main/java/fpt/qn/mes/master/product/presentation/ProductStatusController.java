package fpt.qn.mes.master.product.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.create.CreateProductStatusRequest;
import fpt.qn.mes.master.product.application.dto.productstatus.search.ProductStatusSearchRequest;
import fpt.qn.mes.master.product.application.port.in.ProductStatusUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/product-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStatusController {

    ProductStatusUseCase productStatusUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ProductStatusResponse>>> getProductStatuses(
            @ModelAttribute ProductStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productStatusUseCase.getProductStatuses(request), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createProductStatus(
            @Valid @RequestBody CreateProductStatusRequest request) {
        productStatusUseCase.createProductStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
