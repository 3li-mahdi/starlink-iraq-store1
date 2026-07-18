package com.starlinkiraq.store.dto.auth;

/**
 * استجابة تسجيل الدخول/التسجيل. access token يُرجَع بالـ body، بينما refresh token
 * يُرسَل عبر httpOnly cookie منفصل ولا يظهر هنا إطلاقاً.
 */
public record AuthResponse(
        String accessToken,
        UserResponse user
) {
}
