package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.wishlist.WishlistResponse;
import com.starlinkiraq.store.security.UserPrincipal;
import com.starlinkiraq.store.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * يوفّر endpoints قائمة الرغبات (Wishlist)، تتطلب جميعها تسجيل دخول.
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "إدارة قائمة رغبات المستخدم")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "جلب قائمة رغبات المستخدم")
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(wishlistService.getWishlist(principal.getId()));
    }

    @Operation(summary = "إضافة منتج لقائمة الرغبات")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToWishlist(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long productId) {
        wishlistService.addToWishlist(principal.getId(), productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "حذف منتج من قائمة الرغبات")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable Long productId) {
        wishlistService.removeFromWishlist(principal.getId(), productId);
        return ResponseEntity.noContent().build();
    }
}
