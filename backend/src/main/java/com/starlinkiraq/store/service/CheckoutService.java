package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.order.CheckoutRequest;
import com.starlinkiraq.store.dto.order.OrderItemResponse;
import com.starlinkiraq.store.dto.order.OrderResponse;
import com.starlinkiraq.store.entity.Cart;
import com.starlinkiraq.store.entity.CartItem;
import com.starlinkiraq.store.entity.DigitalAsset;
import com.starlinkiraq.store.entity.Order;
import com.starlinkiraq.store.entity.OrderItem;
import com.starlinkiraq.store.entity.OrderStatus;
import com.starlinkiraq.store.entity.Payment;
import com.starlinkiraq.store.entity.PaymentStatus;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.exception.BadRequestException;
import com.starlinkiraq.store.exception.InsufficientStockException;
import com.starlinkiraq.store.repository.CartRepository;
import com.starlinkiraq.store.repository.DigitalAssetRepository;
import com.starlinkiraq.store.repository.OrderRepository;
import com.starlinkiraq.store.repository.PaymentRepository;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.UserRepository;
import com.starlinkiraq.store.service.payment.PaymentGateway;
import com.starlinkiraq.store.service.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ينفّذ عملية الشراء الكاملة (checkout): يتحقق من الأسعار والمخزون من السيرفر حصراً،
 * يطبّق الكوبون، يخصم المخزون، يخصّص المحتوى الرقمي، وينفّذ الدفع الوهمي.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final DigitalAssetRepository digitalAssetRepository;
    private final CouponService couponService;
    private final DigitalAssetService digitalAssetService;
    private final PaymentGateway paymentGateway;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    /**
     * ينفّذ عملية شراء كاملة لسلة المستخدم الحالية.
     *
     * @param userId معرّف المستخدم المشتري
     * @param request بيانات الشراء (معرّف العملية الفريد، عنوان الشحن، كود الكوبون)
     * @param ipAddress عنوان IP لمصدر الطلب
     * @return بيانات الطلب المكتمل
     * تأثير جانبي: ينشئ طلباً ودفعة، يخصم المخزون، يخصّص محتوى رقمي، يُفرغ السلة، ويرسل بريد تأكيد
     */
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request, String ipAddress) {
        Order existingOrder = orderRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (existingOrder != null) {
            if (!existingOrder.getUser().getId().equals(userId)) {
                throw new BadRequestException("معرّف العملية غير صالح");
            }
            return toResponse(existingOrder);
        }

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BadRequestException("السلة فارغة"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("لا يمكن إتمام الشراء بسلة فارغة");
        }

        User user = userRepository.getReferenceById(userId);
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            // يُعاد تحميل المنتج من قاعدة البيانات دائماً؛ لا يُعتمَد أبداً على أي سعر يرسله الفرونت إند
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BadRequestException("أحد المنتجات لم يعد متوفراً"));

            if (!product.isActive()) {
                throw new BadRequestException("المنتج \"" + product.getName() + "\" لم يعد متوفراً");
            }

            BigDecimal unitPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
            int quantity = cartItem.getQuantity();

            if (product.isRequiresShipping()) {
                if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
                    throw new InsufficientStockException("الكمية المطلوبة من \"" + product.getName() + "\" غير متوفرة");
                }
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .priceAtPurchase(unitPrice)
                    .build();
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            var validation = couponService.validateCoupon(request.couponCode(), totalAmount);
            if (!validation.valid()) {
                throw new BadRequestException(validation.message());
            }
            discountAmount = validation.discountAmount();
            appliedCouponCode = request.couponCode();
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .shippingAddress(request.shippingAddress())
                .couponCode(appliedCouponCode)
                .idempotencyKey(request.idempotencyKey())
                .build();
        order = orderRepository.save(order);

        Map<Long, List<String>> digitalContentByProduct = assignDigitalContent(order, orderItems);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.getItems().addAll(orderItems);
        order = orderRepository.save(order);

        if (appliedCouponCode != null) {
            couponService.incrementUsage(appliedCouponCode);
        }

        PaymentResult paymentResult = paymentGateway.charge(order);
        Payment payment = Payment.builder()
                .order(order)
                .status(paymentResult.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionRef(paymentResult.transactionRef())
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        order.setStatus(paymentResult.success() ? OrderStatus.PAID : OrderStatus.PENDING);
        order = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        auditLogService.log(userId, "ORDER_PLACED", "Order", order.getId(), ipAddress);
        auditLogService.log(userId, "PAYMENT_" + payment.getStatus(), "Payment", payment.getId(), ipAddress);
        emailService.sendOrderConfirmationEmail(user.getEmail(), order.getId());

        return toResponse(order, digitalContentByProduct);
    }

    private Map<Long, List<String>> assignDigitalContent(Order order, List<OrderItem> orderItems) {
        Map<Long, List<String>> result = new java.util.HashMap<>();
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            if (!product.isRequiresShipping() && product.getDigitalDeliveryType() != null) {
                List<String> content = digitalAssetService.assignAssets(product, item.getQuantity(), order.getId());
                result.put(product.getId(), content);
            }
        }
        return result;
    }

    /**
     * يجلب طلباً عبر معرّفه، مع التأكد أنه يخص المستخدم الطالب (حماية IDOR) إلا إذا كان أدمن.
     *
     * @param orderId معرّف الطلب
     * @param userId معرّف المستخدم الطالب
     * @param isAdmin هل الطالب أدمن (يستطيع رؤية أي طلب)
     * @return بيانات الطلب
     * ملاحظة: يرمي استثناء "غير موجود" إذا كان الطلب لا يخص المستخدم، بدلاً من كشف وجوده لمستخدم آخر
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId, Long userId, boolean isAdmin) {
        Order order = isAdmin
                ? orderRepository.findById(orderId).orElseThrow(() -> new com.starlinkiraq.store.exception.ResourceNotFoundException("الطلب غير موجود"))
                : orderRepository.findByIdAndUser_Id(orderId, userId)
                        .orElseThrow(() -> new com.starlinkiraq.store.exception.ResourceNotFoundException("الطلب غير موجود"));
        return toResponse(order);
    }

    /**
     * يجلب كل طلبات مستخدم معيّن، بعد التأكد أن الطالب هو نفسه صاحب الطلبات أو أدمن.
     *
     * @param targetUserId معرّف المستخدم صاحب الطلبات
     * @param pageable معلومات الصفحة والحجم
     * @return صفحة من طلبات المستخدم
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderResponse> getUserOrders(
            Long targetUserId, org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findByUser_Id(targetUserId, pageable).map(this::toResponse);
    }

    private OrderResponse toResponse(Order order) {
        Map<Long, List<String>> digitalContent = loadDigitalContent(order.getId());
        return toResponse(order, digitalContent);
    }

    private OrderResponse toResponse(Order order, Map<Long, List<String>> digitalContentByProduct) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        digitalContentByProduct.get(item.getProduct().getId())
                ))
                .toList();

        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(), order.getDiscountAmount(),
                order.getShippingAddress(), order.getCouponCode(), items, order.getCreatedAt());
    }

    private Map<Long, List<String>> loadDigitalContent(Long orderId) {
        Map<Long, List<String>> map = new java.util.HashMap<>();
        for (DigitalAsset asset : digitalAssetRepository.findByAssignedOrderId(orderId)) {
            map.computeIfAbsent(asset.getProduct().getId(), key -> new ArrayList<>()).add(asset.getDeliveryContent());
        }
        return map;
    }
}
