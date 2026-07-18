package com.starlinkiraq.store.dto.auth;

/**
 * نتيجة داخلية لعملية مصادقة ناجحة، تحمل التوكنين لتمرير refresh token للتحكم كـ httpOnly cookie
 * دون تسريبه في أي استجابة JSON.
 */
public record AuthResult(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
