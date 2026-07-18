package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.product.AddDigitalAssetsRequest;
import com.starlinkiraq.store.dto.product.ProductRequest;
import com.starlinkiraq.store.dto.product.ProductResponse;
import com.starlinkiraq.store.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * عمليات إدارة المنتجات، متاحة للأدمن فقط.
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Products", description = "إدارة المنتجات (للأدمن فقط)")
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "إضافة منتج جديد")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @Operation(summary = "تعديل منتج موجود")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "حذف (إلغاء تفعيل) منتج")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "إضافة نسخ محتوى رقمي جديدة (أكواد تفعيل/روابط) لمنتج رقمي")
    @PostMapping("/{id}/digital-assets")
    public ResponseEntity<Void> addDigitalAssets(@PathVariable Long id,
                                                   @Valid @RequestBody AddDigitalAssetsRequest request) {
        productService.addDigitalAssets(id, request);
        return ResponseEntity.noContent().build();
    }
}
