package com.starlinkiraq.store.security;

import com.starlinkiraq.store.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * يدير إنشاء وقراءة وحذف كوكي refresh token الآمن (httpOnly + Secure + SameSite=Strict).
 */
@Component
@RequiredArgsConstructor
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AppProperties appProperties;

    /**
     * يضيف كوكي refresh token لاستجابة HTTP بأمان (httpOnly، Secure، SameSite=Strict).
     *
     * @param response استجابة HTTP التي سيُضاف لها الكوكي
     * @param refreshToken قيمة رمز التجديد
     * تأثير جانبي: يضيف رأس Set-Cookie للاستجابة
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        long maxAgeSeconds = appProperties.getJwt().getRefreshTokenExpirationMs() / 1000;
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * يمسح كوكي refresh token عند تسجيل الخروج.
     *
     * @param response استجابة HTTP التي سيُمسح منها الكوكي
     * تأثير جانبي: يضيف رأس Set-Cookie بعمر صفري للاستجابة
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * يستخرج قيمة refresh token من كوكيز الطلب الوارد.
     *
     * @param request طلب HTTP الوارد
     * @return قيمة الرمز إن وُجدت
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
