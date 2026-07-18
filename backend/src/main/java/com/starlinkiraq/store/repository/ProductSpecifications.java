package com.starlinkiraq.store.repository;

import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.ProductType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * يبني شروط بحث وفلترة ديناميكية للمنتجات (فئة، سعر، نوع، نص بحث) تُستخدم مع JpaSpecificationExecutor.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    /**
     * يبني Specification يجمع كل شروط الفلترة الاختيارية المرسلة من المستخدم.
     *
     * @param search نص بحث حر يُطابَق مع اسم المنتج (اختياري)
     * @param category الفئة المطلوب الفلترة عليها (اختياري)
     * @param minPrice أقل سعر مقبول (اختياري)
     * @param maxPrice أعلى سعر مقبول (اختياري)
     * @param productType نوع المنتج مادي/رقمي (اختياري)
     * @param activeOnly عندما تكون true تُستبعد المنتجات غير المفعّلة
     * @return Specification جاهز للاستخدام مع الـ repository
     */
    public static Specification<Product> withFilters(String search, String category, BigDecimal minPrice,
                                                       BigDecimal maxPrice, ProductType productType,
                                                       boolean activeOnly) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (activeOnly) {
                predicates = cb.and(predicates, cb.isTrue(root.get("isActive")));
            }
            if (search != null && !search.isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            if (category != null && !category.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            }
            if (minPrice != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (productType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("productType"), productType));
            }
            return predicates;
        };
    }
}
