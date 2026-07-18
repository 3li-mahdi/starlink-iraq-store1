package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.cart.AddCartItemRequest;
import com.starlinkiraq.store.dto.cart.CartResponse;
import com.starlinkiraq.store.dto.cart.UpdateCartItemRequest;
import com.starlinkiraq.store.security.UserPrincipal;
import com.starlinkiraq.store.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * يوفّر endpoints سلة التسوق، تدعم المستخدم المسجَّل والزائر (guest عبر رأس X-Guest-Session-Id).
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "إدارة سلة التسوق")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "جلب سلة التسوق الحالية")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionToken) {
        return ResponseEntity.ok(cartService.getCart(userIdOf(principal), guestSessionToken));
    }

    @Operation(summary = "إضافة منتج للسلة")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionToken,
                                                 @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userIdOf(principal), guestSessionToken, request));
    }

    @Operation(summary = "تحديث كمية عنصر بالسلة")
    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateItem(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionToken,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userIdOf(principal), guestSessionToken, id, request));
    }

    @Operation(summary = "حذف عنصر من السلة")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionToken,
                                                     @PathVariable Long id) {
        return ResponseEntity.ok(cartService.removeItem(userIdOf(principal), guestSessionToken, id));
    }

    private Long userIdOf(UserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }
}
