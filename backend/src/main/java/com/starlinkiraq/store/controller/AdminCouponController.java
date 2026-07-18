package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.coupon.CouponRequest;
import com.starlinkiraq.store.dto.coupon.CouponResponse;
import com.starlinkiraq.store.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * عمليات إدارة كوبونات الخصم، متاحة للأدمن فقط.
 */
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Coupons", description = "إدارة كوبونات الخصم (للأدمن فقط)")
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "إنشاء كوبون خصم جديد")
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @Operation(summary = "جلب كل الكوبونات")
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @Operation(summary = "تفعيل أو تعطيل كوبون")
    @PutMapping("/{id}/active")
    public ResponseEntity<CouponResponse> setActive(@PathVariable Long id, @RequestParam boolean isActive) {
        return ResponseEntity.ok(couponService.setCouponActive(id, isActive));
    }
}
