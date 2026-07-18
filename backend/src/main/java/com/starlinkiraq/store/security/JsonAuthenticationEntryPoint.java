package com.starlinkiraq.store.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlinkiraq.store.dto.common.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * يُعيد استجابة JSON موحدة عند محاولة الوصول لمورد محمي بدون مصادقة صالحة.
 */
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                          @NonNull AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        ApiErrorResponse body = new ApiErrorResponse("يجب تسجيل الدخول للوصول لهذا المورد",
                HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
