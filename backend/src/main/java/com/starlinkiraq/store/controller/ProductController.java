package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.common.PageResponse;
import com.starlinkiraq.store.dto.product.ProductResponse;
import com.starlinkiraq.store.dto.product.ProductVariantResponse;
import com.starlinkiraq.store.dto.review.ReviewRequest;
import com.starlinkiraq.store.dto.review.ReviewResponse;
import com.starlinkiraq.store.entity.ProductType;
import com.starlinkiraq.store.security.UserPrincipal;
import com.starlinkiraq.store.service.ProductService;
import com.starlinkiraq.store.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * يوفّر endpoints عرض المنتجات العامة (بحث، فلترة، صفحات) ومراجعاتها.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "استعراض المنتجات والمراجعات")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @Operation(summary = "جلب قائمة المنتجات مع دعم البحث والفلترة والصفحات")
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductType productType,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = productService.getProducts(search, category, minPrice, maxPrice, productType, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @Operation(summary = "جلب تفاصيل منتج واحد")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @Operation(summary = "جلب منتجات ذات صلة (نفس الفئة) - قد يعجبك أيضاً")
    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductResponse>> getRelatedProducts(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getRelatedProducts(id, 4));
    }

    @Operation(summary = "جلب كل موديلات مجموعة منتج معيّن (مثل Mini/X/Standard) لعرضها كقائمة اختيار")
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<ProductVariantResponse>> getVariants(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getVariants(id));
    }

    @Operation(summary = "جلب المراجعات المعتمدة لمنتج معيّن")
    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
            @PathVariable Long id, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(reviewService.getApprovedReviews(id, pageable)));
    }

    @Operation(summary = "إضافة مراجعة لمنتج (يتطلب تسجيل دخول)")
    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long id,
                                                      @Valid @RequestBody ReviewRequest request,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        ReviewResponse response = reviewService.submitReview(principal.getId(), id, request);
        return ResponseEntity.ok(response);
    }
}
