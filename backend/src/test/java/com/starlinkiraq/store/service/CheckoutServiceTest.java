package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.order.CheckoutRequest;
import com.starlinkiraq.store.dto.order.OrderResponse;
import com.starlinkiraq.store.entity.Cart;
import com.starlinkiraq.store.entity.CartItem;
import com.starlinkiraq.store.entity.Order;
import com.starlinkiraq.store.entity.OrderStatus;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.ProductType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * اختبارات وحدة لخدمة إتمام الشراء: التحقق من السعر من السيرفر حصراً، منع الطلب المكرر، والتحقق من المخزون.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DigitalAssetRepository digitalAssetRepository;
    @Mock
    private CouponService couponService;
    @Mock
    private DigitalAssetService digitalAssetService;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailService emailService;

    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(cartRepository, productRepository, orderRepository, paymentRepository,
                userRepository, digitalAssetRepository, couponService, digitalAssetService, paymentGateway,
                auditLogService, emailService);
    }

    private User buildUser() {
        return User.builder().id(1L).fullName("زبون").email("customer@example.com").build();
    }

    private Product buildProduct(Long id, BigDecimal price, Integer stock) {
        return Product.builder()
                .id(id).name("Starlink Kit").price(price).productType(ProductType.PHYSICAL)
                .requiresShipping(true).stockQuantity(stock).category("kits").isActive(true)
                .build();
    }

    @Test
    void checkout_shouldReturnExistingOrder_whenIdempotencyKeyAlreadyUsed() {
        User user = buildUser();
        Order existingOrder = Order.builder()
                .id(500L).user(user).status(OrderStatus.PAID).totalAmount(BigDecimal.valueOf(300))
                .discountAmount(BigDecimal.ZERO).idempotencyKey("dup-key").items(new ArrayList<>())
                .build();
        when(orderRepository.findByIdempotencyKey("dup-key")).thenReturn(Optional.of(existingOrder));
        when(digitalAssetRepository.findByAssignedOrderId(500L)).thenReturn(Collections.emptyList());

        CheckoutRequest request = new CheckoutRequest("dup-key", "بغداد", null);
        OrderResponse response = checkoutService.checkout(1L, request, "127.0.0.1");

        assertThat(response.id()).isEqualTo(500L);
        verify(cartRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void checkout_shouldThrowBadRequest_whenCartIsEmpty() {
        when(orderRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        Cart emptyCart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(emptyCart));

        CheckoutRequest request = new CheckoutRequest("key-1", "بغداد", null);

        assertThatThrownBy(() -> checkoutService.checkout(1L, request, "127.0.0.1"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void checkout_shouldUseServerSidePrice_ignoringAnyClientSuppliedPrice() {
        User user = buildUser();
        Product product = buildProduct(10L, BigDecimal.valueOf(750), 5);
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(item);

        when(orderRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) {
                o.setId(999L);
            }
            return o;
        });
        when(paymentGateway.charge(any(Order.class))).thenReturn(new PaymentResult(true, "REF-1", "ok"));

        CheckoutRequest request = new CheckoutRequest("key-2", "بغداد", null);
        OrderResponse response = checkoutService.checkout(1L, request, "127.0.0.1");

        // السعر الإجمالي = 750 * 2 = 1500 بناءً على سعر قاعدة البيانات فقط، بغض النظر عن أي سعر آخر
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void checkout_shouldThrowInsufficientStock_whenStockTooLow() {
        User user = buildUser();
        Product product = buildProduct(10L, BigDecimal.valueOf(750), 1);
        Cart cart = Cart.builder().id(1L).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product).quantity(5).build();
        cart.getItems().add(item);

        when(orderRepository.findByIdempotencyKey("key-3")).thenReturn(Optional.empty());
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        CheckoutRequest request = new CheckoutRequest("key-3", "بغداد", null);

        assertThatThrownBy(() -> checkoutService.checkout(1L, request, "127.0.0.1"))
                .isInstanceOf(InsufficientStockException.class);
    }
}
