package com.starlinkiraq.store.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * اختبارات تكامل لدورة المصادقة الكاملة: التسجيل، تسجيل الدخول، الوصول المحمي، وقفل الحساب.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_thenAccessProtectedEndpoint_shouldSucceedWithToken() throws Exception {
        Map<String, String> registerBody = Map.of(
                "fullName", "أحمد علي",
                "email", "ahmed@example.com",
                "password", "Password123",
                "phoneNumber", "07701234567"
        );

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.email").value("ahmed@example.com"))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();

        mockMvc.perform(get("/api/wishlist").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void register_withDuplicateEmail_shouldReturn409() throws Exception {
        Map<String, String> registerBody = Map.of(
                "fullName", "مستخدم أول",
                "email", "duplicate@example.com",
                "password", "Password123",
                "phoneNumber", ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withWeakPassword_shouldReturn400ValidationError() throws Exception {
        Map<String, String> registerBody = Map.of(
                "fullName", "مستخدم",
                "email", "weak@example.com",
                "password", "123",
                "phoneNumber", ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_withWrongPasswordRepeatedly_shouldLockAccount() throws Exception {
        Map<String, String> registerBody = Map.of(
                "fullName", "زبون القفل",
                "email", "lockme@example.com",
                "password", "Password123",
                "phoneNumber", ""
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk());

        Map<String, String> wrongLogin = Map.of("email", "lockme@example.com", "password", "WrongPassword1");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isUnauthorized());
        }

        // بعد 5 محاولات فاشلة يجب أن يُقفَل الحساب حتى مع كلمة مرور صحيحة
        Map<String, String> correctLogin = Map.of("email", "lockme@example.com", "password", "Password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isLocked());
    }

    @Test
    void sqlInjectionAttempt_inLoginEmail_shouldBeRejectedSafely() throws Exception {
        Map<String, String> maliciousLogin = Map.of(
                "email", "' OR '1'='1",
                "password", "anything"
        );

        // القيمة ليست بريداً صحيحاً، يجب أن يرفضها التحقق (Bean Validation) بأمان دون أي خطأ بالسيرفر
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(maliciousLogin)))
                .andExpect(status().isBadRequest());
    }
}
