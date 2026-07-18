package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.cart.AddCartItemRequest;
import com.starlinkiraq.store.dto.cart.CartItemResponse;
import com.starlinkiraq.store.dto.cart.CartResponse;
import com.starlinkiraq.store.dto.cart.UpdateCartItemRequest;
import com.starlinkiraq.store.entity.Cart;
import com.starlinkiraq.store.entity.CartItem;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.exception.BadRequestException;
import com.starlinkiraq.store.exception.InsufficientStockException;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.CartItemRepository;
import com.starlinkiraq.store.repository.CartRepository;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * يدير سلة التسوق لكل من المستخدم المسجَّل والزائر (guest عبر session token)،
 * مع تحديث فوري للسعر الإجمالي والتحقق من توفر المخزون.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * يجلب سلة المستخدم أو الزائر الحالية، مع إنشائها إذا لم تكن موجودة.
     *
     * @param userId معرّف المستخدم المسجَّل دخوله (null إذا كان زائراً)
     * @param guestSessionToken معرّف جلسة الزائر (null إذا كان مستخدماً مسجَّلاً)
     * @return بيانات السلة والعناصر والسعر الإجمالي
     */
    @Transactional
    public CartResponse getCart(Long userId, String guestSessionToken) {
        Cart cart = getOrCreateCart(userId, guestSessionToken);
        return toResponse(cart);
    }

    /**
     * يضيف منتجاً للسلة، أو يزيد كميته إذا كان موجوداً مسبقاً، بعد التحقق من توفر الكمية بالمخزون.
     *
     * @param userId معرّف المستخدم المسجَّل دخوله (null إذا كان زائراً)
     * @param guestSessionToken معرّف جلسة الزائر (null إذا كان مستخدماً مسجَّلاً)
     * @param request المنتج المطلوب إضافته والكمية
     * @return بيانات السلة بعد الإضافة
     * تأثير جانبي: يعدّل جدول cart_items بقاعدة البيانات
     */
    @Transactional
    public CartResponse addItem(Long userId, String guestSessionToken, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId, guestSessionToken);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("المنتج غير موجود"));

        if (!product.isActive()) {
            throw new BadRequestException("هذا المنتج غير متوفر حالياً");
        }

        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElse(null);
        int newQuantity = (item != null ? item.getQuantity() : 0) + request.quantity();
        validateStock(product, newQuantity);

        if (item != null) {
            item.setQuantity(newQuantity);
        } else {
            item = CartItem.builder().cart(cart).product(product).quantity(newQuantity).build();
            cart.getItems().add(item);
        }
        cartItemRepository.save(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    /**
     * يحدّث كمية عنصر موجود بالسلة.
     *
     * @param userId معرّف المستخدم المسجَّل دخوله (null إذا كان زائراً)
     * @param guestSessionToken معرّف جلسة الزائر (null إذا كان مستخدماً مسجَّلاً)
     * @param itemId معرّف عنصر السلة
     * @param request الكمية الجديدة
     * @return بيانات السلة بعد التحديث
     * تأثير جانبي: يعدّل صف بجدول cart_items
     */
    @Transactional
    public CartResponse updateItem(Long userId, String guestSessionToken, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId, guestSessionToken);
        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("عنصر السلة غير موجود"));

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    /**
     * يحذف عنصراً من السلة.
     *
     * @param userId معرّف المستخدم المسجَّل دخوله (null إذا كان زائراً)
     * @param guestSessionToken معرّف جلسة الزائر (null إذا كان مستخدماً مسجَّلاً)
     * @param itemId معرّف عنصر السلة
     * @return بيانات السلة بعد الحذف
     * تأثير جانبي: يحذف صفاً من جدول cart_items
     */
    @Transactional
    public CartResponse removeItem(Long userId, String guestSessionToken, Long itemId) {
        Cart cart = getOrCreateCart(userId, guestSessionToken);
        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("عنصر السلة غير موجود"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    /**
     * يدمج سلة الزائر مع سلة المستخدم بعد تسجيل الدخول، حتى لا تُفقَد المنتجات التي أضافها قبل الدخول.
     *
     * @param guestSessionToken معرّف جلسة الزائر السابقة
     * @param userId معرّف المستخدم الذي سجّل دخوله للتو
     * تأثير جانبي: ينقل عناصر سلة الزائر لسلة المستخدم ويحذف سلة الزائر
     */
    @Transactional
    public void mergeGuestCartIntoUser(String guestSessionToken, Long userId) {
        if (guestSessionToken == null || guestSessionToken.isBlank()) {
            return;
        }
        Cart guestCart = cartRepository.findByGuestSessionToken(guestSessionToken).orElse(null);
        if (guestCart == null || guestCart.getItems().isEmpty()) {
            return;
        }

        Cart userCart = getOrCreateCart(userId, null);
        for (CartItem guestItem : List.copyOf(guestCart.getItems())) {
            CartItem existing = cartItemRepository
                    .findByCart_IdAndProduct_Id(userCart.getId(), guestItem.getProduct().getId())
                    .orElse(null);
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(existing);
            } else {
                guestItem.setCart(userCart);
                cartItemRepository.save(guestItem);
            }
        }
        cartRepository.delete(guestCart);
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.isRequiresShipping() && product.getStockQuantity() != null
                && requestedQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException("الكمية المطلوبة غير متوفرة بالمخزون");
        }
    }

    private Cart getOrCreateCart(Long userId, String guestSessionToken) {
        if (userId != null) {
            return cartRepository.findByUser_Id(userId).orElseGet(() -> {
                User user = userRepository.getReferenceById(userId);
                return cartRepository.save(Cart.builder().user(user).build());
            });
        }
        if (guestSessionToken == null || guestSessionToken.isBlank()) {
            throw new BadRequestException("معرّف جلسة الزائر مطلوب");
        }
        return cartRepository.findByGuestSessionToken(guestSessionToken)
                .orElseGet(() -> cartRepository.save(Cart.builder().guestSessionToken(guestSessionToken).build()));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        effectivePrice(item.getProduct()),
                        item.getQuantity(),
                        effectivePrice(item.getProduct()).multiply(BigDecimal.valueOf(item.getQuantity())),
                        item.getProduct().getStockQuantity()
                ))
                .toList();

        BigDecimal total = items.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }

    private BigDecimal effectivePrice(Product product) {
        return product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
    }
}
