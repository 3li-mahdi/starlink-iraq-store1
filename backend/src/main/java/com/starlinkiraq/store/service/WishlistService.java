package com.starlinkiraq.store.service;

import com.starlinkiraq.store.config.AppProperties;
import com.starlinkiraq.store.dto.product.ProductResponse;
import com.starlinkiraq.store.dto.wishlist.WishlistResponse;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.entity.Wishlist;
import com.starlinkiraq.store.exception.ConflictException;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.UserRepository;
import com.starlinkiraq.store.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * يدير قائمة رغبات المستخدم: عرض، إضافة، وحذف منتجات محفوظة لوقت لاحق.
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    /**
     * يجلب كل المنتجات المحفوظة بقائمة رغبات المستخدم.
     *
     * @param userId معرّف المستخدم
     * @return قائمة عناصر الـ wishlist مع بيانات كل منتج
     */
    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(Long userId) {
        return wishlistRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * يضيف منتجاً لقائمة رغبات المستخدم.
     *
     * @param userId معرّف المستخدم
     * @param productId معرّف المنتج
     * تأثير جانبي: يضيف سجلاً جديداً بجدول wishlists
     */
    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ConflictException("المنتج موجود بقائمة رغباتك مسبقاً");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("المنتج غير موجود"));
        User user = userRepository.getReferenceById(userId);

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .addedAt(LocalDateTime.now())
                .build();
        wishlistRepository.save(wishlist);
    }

    /**
     * يحذف منتجاً من قائمة رغبات المستخدم.
     *
     * @param userId معرّف المستخدم
     * @param productId معرّف المنتج
     * تأثير جانبي: يحذف سجلاً من جدول wishlists
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        if (!wishlistRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ResourceNotFoundException("المنتج غير موجود بقائمة رغباتك");
        }
        wishlistRepository.deleteByUser_IdAndProduct_Id(userId, productId);
    }

    private WishlistResponse toResponse(Wishlist wishlist) {
        ProductResponse productResponse = ProductResponse.from(wishlist.getProduct(), appProperties.getLowStockThreshold());
        return new WishlistResponse(wishlist.getId(), productResponse, wishlist.getAddedAt());
    }
}
