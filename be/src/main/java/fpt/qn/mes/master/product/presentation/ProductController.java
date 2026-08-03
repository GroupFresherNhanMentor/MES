package fpt.qn.mes.master.product.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.application.dto.product.create.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.product.search.ProductSearchRequest;
import fpt.qn.mes.master.product.application.dto.product.update.UpdateProductRequest;
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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productUseCase.getProducts(request), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productUseCase.getProductById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        productUseCase.createProduct(request);
        return ResponseEntity.status(201).body(ApiResponse.success("Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        productUseCase.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateProduct(@PathVariable UUID id) {
        productUseCase.activateProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable UUID id) {
        productUseCase.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }
}
