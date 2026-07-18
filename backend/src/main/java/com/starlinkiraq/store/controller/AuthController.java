package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.auth.AuthResponse;
import com.starlinkiraq.store.dto.auth.AuthResult;
import com.starlinkiraq.store.dto.auth.LoginRequest;
import com.starlinkiraq.store.dto.auth.RegisterRequest;
import com.starlinkiraq.store.dto.auth.VerifyEmailRequest;
import com.starlinkiraq.store.security.CookieUtil;
import com.starlinkiraq.store.security.IpAddressUtil;
import com.starlinkiraq.store.security.UserPrincipal;
import com.starlinkiraq.store.service.AuthService;
import com.starlinkiraq.store.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * يوفّر endpoints المصادقة: التسجيل، تسجيل الدخول، تجديد التوكن، تسجيل الخروج، وتفعيل البريد الإلكتروني.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "تسجيل الدخول والتسجيل وإدارة الجلسة")
public class AuthController {

    private final AuthService authService;
    private final CartService cartService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "تسجيل مستخدم جديد")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionToken,
                                                  HttpServletRequest httpRequest,
                                                  HttpServletResponse httpResponse) {
        AuthResult result = authService.register(request, IpAddressUtil.resolveClientIp(httpRequest));
        mergeGuestCartIfPresent(guestSessionToken, result);
        cookieUtil.addRefreshTokenCookie(httpResponse, result.refreshToken());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.user()));
    }

    @Operation(summary = "تسجيل الدخول")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionToken,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        AuthResult result = authService.login(request, IpAddressUtil.resolveClientIp(httpRequest));
        mergeGuestCartIfPresent(guestSessionToken, result);
        cookieUtil.addRefreshTokenCookie(httpResponse, result.refreshToken());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.user()));
    }

    @Operation(summary = "تجديد access token باستخدام refresh token المخزّن بالكوكي")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String rawRefreshToken = cookieUtil.extractRefreshToken(httpRequest).orElse(null);
        AuthResult result = authService.refresh(rawRefreshToken);
        cookieUtil.addRefreshTokenCookie(httpResponse, result.refreshToken());
        return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.user()));
    }

    @Operation(summary = "تسجيل الخروج وإبطال refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse httpResponse) {
        String rawRefreshToken = cookieUtil.extractRefreshToken(httpRequest).orElse(null);
        Long userId = principal != null ? principal.getId() : null;
        authService.logout(rawRefreshToken, userId, IpAddressUtil.resolveClientIp(httpRequest));
        cookieUtil.clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "تفعيل البريد الإلكتروني عبر رمز التفعيل")
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.noContent().build();
    }

    private void mergeGuestCartIfPresent(String guestSessionToken, AuthResult result) {
        if (guestSessionToken != null && !guestSessionToken.isBlank()) {
            cartService.mergeGuestCartIntoUser(guestSessionToken, extractUserId(result));
        }
    }

    private Long extractUserId(AuthResult result) {
        return result.user().id();
    }
}
