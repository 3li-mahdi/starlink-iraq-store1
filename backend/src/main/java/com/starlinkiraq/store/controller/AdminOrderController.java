package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.common.PageResponse;
import com.starlinkiraq.store.dto.order.AdminOrderResponse;
import com.starlinkiraq.store.dto.order.OrderStatusUpdateRequest;
import com.starlinkiraq.store.security.IpAddressUtil;
import com.starlinkiraq.store.security.UserPrincipal;
import com.starlinkiraq.store.service.AdminOrderService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * عمليات إدارة الطلبات، متاحة للأدمن فقط.
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Orders", description = "إدارة الطلبات (للأدمن فقط)")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "جلب كل الطلبات")
    @GetMapping
    public ResponseEntity<PageResponse<AdminOrderResponse>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(adminOrderService.getAllOrders(pageable)));
    }

    @Operation(summary = "تحديث حالة طلب")
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminOrderResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody OrderStatusUpdateRequest request,
                                                             @AuthenticationPrincipal UserPrincipal principal,
                                                             HttpServletRequest httpRequest) {
        var response = adminOrderService.updateOrderStatus(id, request.status(), principal.getId(),
                IpAddressUtil.resolveClientIp(httpRequest));
        return ResponseEntity.ok(response);
    }
}
