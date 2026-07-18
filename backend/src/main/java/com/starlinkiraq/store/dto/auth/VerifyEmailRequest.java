package com.starlinkiraq.store.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "رمز التفعيل مطلوب")
        String token
) {
}
