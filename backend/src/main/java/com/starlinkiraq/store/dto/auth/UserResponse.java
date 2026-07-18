package com.starlinkiraq.store.dto.auth;

import com.starlinkiraq.store.entity.Role;
import com.starlinkiraq.store.entity.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        Role role,
        boolean isEmailVerified
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
                user.getPhoneNumber(), user.getRole(), user.isEmailVerified());
    }
}
