package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.cart.AddCartItemRequest;
import com.starlinkiraq.store.dto.cart.CartResponse;
import com.starlinkiraq.store.entity.Cart;
import com.starlinkiraq.store.entity.CartItem;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.ProductType;
import com.starlinkiraq.store.exception.InsufficientStockException;
import com.starlinkiraq.store.repository.CartItemRepository;
import com.starlinkiraq.store.repository.CartRepository;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * اختبارات وحدة لمنطق سلة التسوق: الإضافة، التحديث، والتحقق من توفر المخزون.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository, productRepository, userRepository);
    }

    private Product buildProduct(Long id, BigDecimal price, Integer stock) {
        return Product.builder()
                .id(id)
                .name("Starlink Dish V2")
                .price(price)
                .productType(ProductType.PHYSICAL)
                .requiresShipping(true)
                .stockQuantity(stock)
                .category("dishes")
                .isActive(true)
                .build();
    }

    @Test
    void addItem_shouldThrowInsufficientStock_whenRequestedQuantityExceedsStock() {
        Cart cart = Cart.builder().id(10L).guestSessionToken("guest-123").items(new ArrayList<>()).build();
        Product product = buildProduct(1L, BigDecimal.valueOf(500), 2);

        when(cartRepository.findByGuestSessionToken("guest-123")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCart_IdAndProduct_Id(10L, 1L)).thenReturn(Optional.empty());

        AddCartItemRequest request = new AddCartItemRequest(1L, 5);

        assertThatThrownBy(() -> cartService.addItem(null, "guest-123", request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addItem_shouldAddNewItem_whenStockIsSufficient() {
        Cart cart = Cart.builder().id(10L).guestSessionToken("guest-123").items(new ArrayList<>()).build();
        Product product = buildProduct(1L, BigDecimal.valueOf(500), 10);

        when(cartRepository.findByGuestSessionToken("guest-123")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCart_IdAndProduct_Id(10L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        AddCartItemRequest request = new AddCartItemRequest(1L, 3);
        CartResponse response = cartService.addItem(null, "guest-123", request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void addItem_shouldAccumulateQuantity_whenItemAlreadyInCart() {
        Cart cart = Cart.builder().id(10L).guestSessionToken("guest-123").items(new ArrayList<>()).build();
        Product product = buildProduct(1L, BigDecimal.valueOf(100), 10);
        CartItem existingItem = CartItem.builder().id(99L).cart(cart).product(product).quantity(2).build();
        cart.getItems().add(existingItem);

        when(cartRepository.findByGuestSessionToken("guest-123")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCart_IdAndProduct_Id(10L, 1L)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        AddCartItemRequest request = new AddCartItemRequest(1L, 3);
        CartResponse response = cartService.addItem(null, "guest-123", request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void getCart_shouldCreateNewUserCart_whenNoneExists() {
        when(cartRepository.findByUser_Id(7L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(7L)).thenReturn(com.starlinkiraq.store.entity.User.builder().id(7L).build());
        Cart newCart = Cart.builder().id(50L).items(new ArrayList<>()).build();
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartResponse response = cartService.getCart(7L, null);

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.items()).isEmpty();
    }
}
