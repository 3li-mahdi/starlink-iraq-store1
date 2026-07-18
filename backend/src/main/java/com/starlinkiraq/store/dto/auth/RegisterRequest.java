package com.starlinkiraq.store.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "الاسم الكامل مطلوب")
        @Size(min = 2, max = 150, message = "الاسم يجب أن يكون بين 2 و150 حرفاً")
        String fullName,

        @NotBlank(message = "البريد الإلكتروني مطلوب")
        @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
        @Size(max = 190)
        String email,

        @NotBlank(message = "كلمة المرور مطلوبة")
        @Size(min = 8, max = 100, message = "كلمة المرور يجب أن تكون 8 أحرف على الأقل")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "كلمة المرور يجب أن تحتوي على حرف كبير وحرف صغير ورقم على الأقل"
        )
        String password,

        @Pattern(regexp = "^$|^[0-9+\\-\\s]{7,20}$", message = "رقم الهاتف غير صحيح")
        String phoneNumber
) {
}
