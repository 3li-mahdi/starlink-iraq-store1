package com.starlinkiraq.store.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * يستخرج عنوان IP الحقيقي للعميل من الطلب، مع مراعاة وجود proxy أمامي.
 */
public final class IpAddressUtil {

    private IpAddressUtil() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
