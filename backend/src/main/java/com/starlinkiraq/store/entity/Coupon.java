package com.starlinkiraq.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * يمثل كوبون خصم يمكن تطبيقه على الطلبات ضمن شروط الحد الأدنى وعدد مرات الاستخدام وتاريخ الانتهاء.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true)
})
public class Coupon extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    private Integer maxUses;

    @Column(nullable = false)
    @Builder.Default
    private int currentUses = 0;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * يتحقق فيما إذا كان الكوبون صالحاً للاستخدام حالياً (مفعّل، لم تنتهِ صلاحيته، ولم يتجاوز حد الاستخدام).
     *
     * @return true إذا كان الكوبون قابلاً للاستخدام الآن
     */
    public boolean isCurrentlyValid() {
        if (!isActive) {
            return false;
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        return maxUses == null || currentUses < maxUses;
    }
}
