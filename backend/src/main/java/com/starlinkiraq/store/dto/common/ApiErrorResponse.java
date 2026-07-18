package com.starlinkiraq.store.dto.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * صيغة موحدة لأي خطأ يُرجعه الـ API، تُستخدم من طرف @ControllerAdvice المركزي.
 */
public record ApiErrorResponse(
        String error,
        int status,
        LocalDateTime timestamp,
        List<String> details
) {
    public ApiErrorResponse(String error, int status, LocalDateTime timestamp) {
        this(error, status, timestamp, null);
    }
}
