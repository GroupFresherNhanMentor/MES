package fpt.qn.mes.master.product.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.service.LookupService;
import fpt.qn.mes.master.product.application.dto.request.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.dto.request.UpdateProductRequest;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductUseCase productUseCase;
    LookupService lookupService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID statusId) {
        var result = (statusId != null)
                ? productUseCase.getProductsByStatus(page, size, statusId)
                : productUseCase.getProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable UUID id) {
        var result = productUseCase.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody CreateProductRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = productUseCase.createProduct(request, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(result, "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable UUID id, @RequestBody UpdateProductRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = productUseCase.updateProduct(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Updated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCT_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable UUID id) {
        productUseCase.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deactivated"));
    }

    @GetMapping("/types")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<java.util.List<fpt.qn.mes.common.service.LookupEntry>>> getProductTypes() {
        return ResponseEntity.ok(ApiResponse.success(lookupService.getProductTypes(), "OK"));
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<java.util.List<fpt.qn.mes.common.service.LookupEntry>>> getProductStatuses() {
        return ResponseEntity.ok(ApiResponse.success(lookupService.getProductStatuses(), "OK"));
    }
}
