package com.starlinkiraq.store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * يفعّل التسجيل التلقائي لتواريخ الإنشاء والتعديل على الكيانات (createdAt/updatedAt).
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
