package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.order.AdminOrderResponse;
import com.starlinkiraq.store.dto.order.OrderItemResponse;
import com.starlinkiraq.store.entity.Order;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * يدير عمليات الطلبات الخاصة بلوحة تحكم الأدمن: استعراض كل الطلبات وتحديث حالتها.
 */
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    /**
     * يجلب كل الطلبات بالنظام لعرضها بلوحة تحكم الأدمن.
     *
     * @param pageable معلومات الصفحة والحجم
     * @return صفحة من كل الطلبات مع بيانات المشتري
     */
    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toAdminResponse);
    }

    /**
     * يحدّث حالة طلب معيّن (مثلاً من PAID إلى SHIPPED) ويُرسل إشعاراً بالبريد الإلكتروني للزبون.
     *
     * @param orderId معرّف الطلب
     * @param newStatus الحالة الجديدة
     * @param adminUserId معرّف الأدمن الذي نفّذ التعديل
     * @param ipAddress عنوان IP لمصدر الطلب
     * @return بيانات الطلب بعد التحديث
     * تأثير جانبي: يعدّل status للطلب، يسجّل الحدث بسجل التدقيق، ويرسل بريد إشعار للزبون
     */
    @Transactional
    public AdminOrderResponse updateOrderStatus(Long orderId, com.starlinkiraq.store.entity.OrderStatus newStatus,
                                                 Long adminUserId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("الطلب غير موجود"));

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        auditLogService.log(adminUserId, "ORDER_STATUS_UPDATED", "Order", order.getId(), ipAddress);
        emailService.sendOrderStatusUpdateEmail(order.getUser().getEmail(), order.getId(), newStatus.name());

        return toAdminResponse(order);
    }

    private AdminOrderResponse toAdminResponse(Order order) {
        var items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getId(), item.getProduct().getId(),
                        item.getProduct().getName(), item.getQuantity(), item.getPriceAtPurchase(), null))
                .toList();

        return new AdminOrderResponse(order.getId(), order.getUser().getId(), order.getUser().getEmail(),
                order.getStatus(), order.getTotalAmount(), order.getDiscountAmount(), order.getShippingAddress(),
                order.getCouponCode(), items, order.getCreatedAt());
    }
}
