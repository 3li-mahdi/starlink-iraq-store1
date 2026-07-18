package com.starlinkiraq.store.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlinkiraq.store.dto.common.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * يُعيد استجابة JSON موحدة عند محاولة مستخدم مصادَق عليه الوصول لمورد لا يملك صلاحية له.
 */
@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                        @NonNull AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        ApiErrorResponse body = new ApiErrorResponse("لا تملك الصلاحية الكافية لتنفيذ هذا الإجراء",
                HttpStatus.FORBIDDEN.value(), LocalDateTime.now());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
