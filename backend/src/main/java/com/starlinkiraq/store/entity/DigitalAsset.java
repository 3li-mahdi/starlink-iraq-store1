package com.starlinkiraq.store.entity;

import com.starlinkiraq.store.security.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * يمثل نسخة فعلية من محتوى رقمي (كود تفعيل، رابط تحميل، بيانات حساب) تُسلَّم لزبون واحد فقط.
 * محتوى التسليم يُخزَّن مشفّراً على مستوى العمود عبر {@link com.starlinkiraq.store.security.EncryptedStringConverter}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "digital_assets", indexes = {
        @Index(name = "idx_digital_asset_product", columnList = "product_id"),
        @Index(name = "idx_digital_asset_used", columnList = "isUsed")
})
public class DigitalAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** المحتوى الفعلي (كود التفعيل أو رابط التحميل أو بيانات الحساب) - مشفّر بقاعدة البيانات. */
    @Lob
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String deliveryContent;

    @Column(nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    private Long assignedOrderId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
