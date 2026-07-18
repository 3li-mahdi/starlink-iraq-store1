package com.starlinkiraq.store.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlinkiraq.store.entity.Role;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * اختبارات تكامل لصلاحيات الوصول (RBAC)، حماية IDOR على الطلبات، وسيناريو شراء كامل بالدفع الوهمي.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderAndAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String registerAndLogin(String email) throws Exception {
        Map<String, String> registerBody = Map.of(
                "fullName", "مستخدم اختبار",
                "email", email,
                "password", "Password123",
                "phoneNumber", ""
        );
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createAdminAndLogin() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@example.com";
        User admin = User.builder()
                .fullName("أدمن المتجر")
                .email(email)
                .password(passwordEncoder.encode("AdminPass123"))
                .role(Role.ADMIN)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();
        userRepository.save(admin);

        Map<String, String> loginBody = Map.of("email", email, "password", "AdminPass123");
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void customer_cannotAccessAdminEndpoint_shouldReturn403() throws Exception {
        String customerToken = registerAndLogin("customer1-" + UUID.randomUUID() + "@example.com");

        Map<String, Object> productBody = Map.of(
                "name", "منتج تجريبي", "description", "وصف", "price", 100,
                "productType", "PHYSICAL", "stockQuantity", 10, "requiresShipping", true,
                "category", "misc", "isActive", true
        );

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_canCreateProduct_andCustomerCanPurchaseIt_withServerCalculatedPrice() throws Exception {
        String adminToken = createAdminAndLogin();
        String customerToken = registerAndLogin("buyer-" + UUID.randomUUID() + "@example.com");

        Map<String, Object> productBody = Map.of(
                "name", "طبق Starlink", "description", "وصف المنتج", "price", 900,
                "productType", "PHYSICAL", "stockQuantity", 5, "requiresShipping", true,
                "category", "dishes", "isActive", true
        );

        String createResponse = mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode productJson = objectMapper.readTree(createResponse);
        long productId = productJson.get("id").asLong();

        // الزبون يضيف المنتج للسلة بكمية 2 - يجب ألا يتأثر السعر النهائي بأي قيمة يرسلها الفرونت إند
        Map<String, Object> addItemBody = Map.of("productId", productId, "quantity", 2);
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(addItemBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1800.0));

        Map<String, String> checkoutBody = Map.of(
                "idempotencyKey", UUID.randomUUID().toString(),
                "shippingAddress", "بغداد - الكرادة"
        );

        String orderResponse = mockMvc.perform(post("/api/checkout")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(checkoutBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.totalAmount").value(1800.0))
                .andReturn().getResponse().getContentAsString();

        long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        // زبون آخر يحاول الوصول لطلب هذا الزبون - يجب أن يُرفض (حماية IDOR)
        String otherCustomerToken = registerAndLogin("stranger-" + UUID.randomUUID() + "@example.com");
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isNotFound());

        // صاحب الطلب نفسه يقدر يشوفه بدون مشكلة
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }
}
