package com.starlinkiraq.store.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * يجمّع كل إعدادات التطبيق المخصصة (app.*) القادمة من متغيرات البيئة أو application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Security security = new Security();
    private final Cors cors = new Cors();
    private final Frontend frontend = new Frontend();
    private final Encryption encryption = new Encryption();
    private int lowStockThreshold = 5;

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
        private String issuer;
    }

    @Getter
    @Setter
    public static class Security {
        private int maxFailedLoginAttempts = 5;
        private int accountLockDurationMinutes = 15;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter
    @Setter
    public static class Frontend {
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Encryption {
        /** مفتاح تشفير الأعمدة الحساسة (مثل محتوى DigitalAsset)، يجب أن يكون 256-bit مشفّر Base64. */
        private String secretKey;
    }
}
