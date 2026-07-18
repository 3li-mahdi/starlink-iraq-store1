package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.common.PageResponse;
import com.starlinkiraq.store.dto.coupon.CouponValidateRequest;
import com.starlinkiraq.store.dto.coupon.CouponValidateResponse;
import com.starlinkiraq.store.dto.order.CheckoutRequest;
import com.starlinkiraq.store.dto.order.OrderResponse;
import com.starlinkiraq.store.security.IpAddressUtil;
import com.starlinkiraq.store.security.UserPrincipal;
import com.starlinkiraq.store.service.CheckoutService;
import com.starlinkiraq.store.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * يوفّر endpoints إتمام الشراء (checkout)، التحقق من الكوبونات، واستعراض الطلبات.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Checkout & Orders", description = "إتمام الشراء والكوبونات والطلبات")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CouponService couponService;

    @Operation(summary = "إتمام عملية الشراء (checkout)")
    @PostMapping("/api/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody CheckoutRequest request,
                                                    HttpServletRequest httpRequest) {
        OrderResponse response = checkoutService.checkout(principal.getId(), request,
                IpAddressUtil.resolveClientIp(httpRequest));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "التحقق من صلاحية كوبون خصم (يتطلب مبلغ الطلب التقريبي)")
    @PostMapping("/api/coupons/validate")
    public ResponseEntity<CouponValidateResponse> validateCoupon(@Valid @RequestBody CouponValidateRequest request,
                                                                    @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") BigDecimal orderAmount) {
        return ResponseEntity.ok(couponService.validateCoupon(request.code(), orderAmount));
    }

    @Operation(summary = "جلب طلب واحد عبر معرّفه (لصاحب الطلب فقط أو الأدمن)")
    @GetMapping("/api/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long id) {
        boolean isAdmin = principal.getUser().getRole().name().equals("ADMIN");
        return ResponseEntity.ok(checkoutService.getOrder(id, principal.getId(), isAdmin));
    }

    @Operation(summary = "جلب كل طلبات مستخدم معيّن (لنفس المستخدم فقط أو الأدمن)")
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    @GetMapping("/api/orders/user/{userId}")
    public ResponseEntity<PageResponse<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(checkoutService.getUserOrders(userId, pageable)));
    }
}
