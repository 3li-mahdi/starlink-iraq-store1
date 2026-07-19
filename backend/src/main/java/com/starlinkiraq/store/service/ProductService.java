package com.starlinkiraq.store.service;

import com.starlinkiraq.store.config.AppProperties;
import com.starlinkiraq.store.dto.product.AddDigitalAssetsRequest;
import com.starlinkiraq.store.dto.product.ProductRequest;
import com.starlinkiraq.store.dto.product.ProductResponse;
import com.starlinkiraq.store.dto.product.ProductVariantResponse;
import com.starlinkiraq.store.entity.DigitalAsset;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.ProductType;
import com.starlinkiraq.store.exception.BadRequestException;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.DigitalAssetRepository;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.ProductSpecifications;
import com.starlinkiraq.store.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * يدير كل عمليات المنتجات: العرض العام مع الفلترة والصفحات، وعمليات الإدارة (إضافة/تعديل/حذف).
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final DigitalAssetRepository digitalAssetRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    /**
     * يجلب قائمة منتجات مفعّلة فقط مع دعم البحث والفلترة والصفحات.
     *
     * @param search نص بحث حر باسم المنتج (اختياري)
     * @param category الفئة المطلوبة (اختياري)
     * @param minPrice أقل سعر (اختياري)
     * @param maxPrice أعلى سعر (اختياري)
     * @param productType نوع المنتج (اختياري)
     * @param pageable معلومات الصفحة والحجم والترتيب
     * @return صفحة من المنتجات المطابقة للفلاتر
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String search, String category, BigDecimal minPrice,
                                              BigDecimal maxPrice, ProductType productType, Pageable pageable) {
        var spec = ProductSpecifications.withFilters(search, category, minPrice, maxPrice, productType, true);
        return productRepository.findAll(spec, pageable)
                .map(product -> ProductResponse.from(product, appProperties.getLowStockThreshold()));
    }

    /**
     * يجلب منتجاً واحداً عبر معرّفه.
     *
     * @param id معرّف المنتج
     * @return بيانات المنتج
     * ملاحظة: يرمي استثناء إذا لم يُوجد المنتج
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = findProductOrThrow(id);
        return ProductResponse.from(product, appProperties.getLowStockThreshold());
    }

    /**
     * يجلب منتجات من نفس فئة منتج معيّن كتوصية "منتجات ذات صلة".
     *
     * @param productId معرّف المنتج الأساسي
     * @param limit أقصى عدد منتجات تُرجَع
     * @return قائمة منتجات مشابهة بنفس الفئة
     */
    @Transactional(readOnly = true)
    public java.util.List<ProductResponse> getRelatedProducts(Long productId, int limit) {
        Product product = findProductOrThrow(productId);
        var spec = ProductSpecifications.withFilters(null, product.getCategory(), null, null, null, true);
        return productRepository.findAll(spec, org.springframework.data.domain.PageRequest.of(0, limit + 1))
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit)
                .map(p -> ProductResponse.from(p, appProperties.getLowStockThreshold()))
                .toList();
    }

    /**
     * يجلب كل موديلات نفس مجموعة منتج معيّن (مثل Mini/X/Standard) لعرضها كقائمة اختيار Dropdown،
     * أو قائمة فارغة إذا كان المنتج بدون موديلات بديلة.
     *
     * @param productId معرّف أحد موديلات المجموعة
     * @return قائمة الموديلات المفعّلة بنفس المجموعة، مرتبة أبجدياً حسب اسم الموديل
     */
    @Transactional(readOnly = true)
    public java.util.List<ProductVariantResponse> getVariants(Long productId) {
        Product product = findProductOrThrow(productId);
        if (product.getVariantGroupKey() == null || product.getVariantGroupKey().isBlank()) {
            return java.util.List.of();
        }
        return productRepository
                .findByVariantGroupKeyAndIsActiveTrueOrderByVariantLabelAsc(product.getVariantGroupKey())
                .stream()
                .map(ProductVariantResponse::from)
                .toList();
    }

    /**
     * يُنشئ منتجاً جديداً (للأدمن فقط).
     *
     * @param request بيانات المنتج
     * @return بيانات المنتج بعد الإنشاء
     * تأثير جانبي: يضيف سجلاً جديداً بجدول products
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = mapToEntity(request, new Product());
        product = productRepository.save(product);
        return ProductResponse.from(product, appProperties.getLowStockThreshold());
    }

    /**
     * يعدّل منتجاً موجوداً (للأدمن فقط). إذا عاد المخزون من صفر لكمية موجبة، يُرسَل إشعار لمن حفظوه بقائمة رغباتهم.
     *
     * @param id معرّف المنتج
     * @param request البيانات الجديدة للمنتج
     * @return بيانات المنتج بعد التعديل
     * تأثير جانبي: يعدّل سجل المنتج، وقد يرسل إشعارات "عودة للمخزون" بالبريد الإلكتروني
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        boolean wasOutOfStock = product.getStockQuantity() != null && product.getStockQuantity() <= 0;

        mapToEntity(request, product);
        product = productRepository.save(product);

        boolean isBackInStock = product.getStockQuantity() != null && product.getStockQuantity() > 0;
        if (wasOutOfStock && isBackInStock) {
            notifyWishlistUsersBackInStock(product);
        }

        return ProductResponse.from(product, appProperties.getLowStockThreshold());
    }

    /**
     * يحذف منتجاً (للأدمن فقط). عملياً يتم إلغاء تفعيله بدلاً من حذفه فعلياً للحفاظ على سلامة سجلات الطلبات القديمة.
     *
     * @param id معرّف المنتج
     * تأثير جانبي: يعدّل حقل isActive للمنتج إلى false
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    /**
     * يضيف نسخاً جديدة من المحتوى الرقمي (أكواد تفعيل/روابط) لمخزون منتج رقمي (للأدمن فقط).
     *
     * @param productId معرّف المنتج الرقمي
     * @param request قائمة المحتوى الرقمي الجديد
     * تأثير جانبي: يضيف سجلات جديدة بجدول digital_assets
     */
    @Transactional
    public void addDigitalAssets(Long productId, AddDigitalAssetsRequest request) {
        Product product = findProductOrThrow(productId);
        if (product.isRequiresShipping()) {
            throw new BadRequestException("لا يمكن إضافة محتوى رقمي لمنتج مادي");
        }
        for (String content : request.contents()) {
            DigitalAsset asset = DigitalAsset.builder()
                    .product(product)
                    .deliveryContent(content)
                    .isUsed(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            digitalAssetRepository.save(asset);
        }
    }

    private void notifyWishlistUsersBackInStock(Product product) {
        wishlistRepository.findByProduct_Id(product.getId())
                .forEach(w -> emailService.sendBackInStockEmail(w.getUser().getEmail(), product.getName()));
    }

    private Product mapToEntity(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setDiscountPrice(request.discountPrice());
        product.setImageUrl(request.imageUrl());
        product.setProductType(request.productType());
        product.setStockQuantity(request.stockQuantity());
        product.setRequiresShipping(request.requiresShipping());
        product.setDigitalDeliveryType(request.digitalDeliveryType());
        product.setCategory(request.category());
        product.setActive(request.isActive());
        product.setVariantGroupKey(blankToNull(request.variantGroupKey()));
        product.setVariantLabel(blankToNull(request.variantLabel()));
        return product;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("المنتج غير موجود"));
    }
}
