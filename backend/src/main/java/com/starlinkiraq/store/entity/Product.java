package com.starlinkiraq.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * يمثل منتجاً في المتجر، سواء كان مادياً (يحتاج شحن) أو رقمياً (يُسلَّم إلكترونياً).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_price", columnList = "price"),
        @Index(name = "idx_product_active", columnList = "isActive"),
        @Index(name = "idx_product_variant_group", columnList = "variantGroupKey")
})
public class Product extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType productType;

    /** الكمية المتوفرة بالمخزون؛ تبقى null للمنتجات الرقمية غير المحدودة الكمية. */
    private Integer stockQuantity;

    @Column(nullable = false)
    @Builder.Default
    private boolean requiresShipping = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DigitalDeliveryType digitalDeliveryType;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /** متوسط التقييم المحسوب من المراجعات المعتمدة (يُحدَّث عند كل مراجعة جديدة). */
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    /**
     * مفتاح يجمع بين موديلات نفس المنتج (مثلاً "starlink-dish" لكل من Mini/X/Standard)،
     * يبقى null إذا كان المنتج بدون موديلات بديلة.
     */
    @Column(length = 100)
    private String variantGroupKey;

    /** الاسم المعروض لهذا الموديل ضمن مجموعته (مثل "Mini"، "X"، "Standard"). */
    @Column(length = 50)
    private String variantLabel;

    /**
     * يتحقق فيما إذا كانت الكمية المتوفرة بالمخزون منخفضة (لعرض تنبيه إلحاح الشراء).
     *
     * @param threshold الحد الأدنى الذي يُعتبر أقل منه المخزون "منخفضاً"
     * @return true إذا كان المنتج مادياً وكميته أقل من أو تساوي الحد المحدد
     */
    public boolean isLowStock(int threshold) {
        return requiresShipping && stockQuantity != null && stockQuantity <= threshold;
    }
}
