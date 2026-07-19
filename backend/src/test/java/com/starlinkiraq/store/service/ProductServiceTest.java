package com.starlinkiraq.store.service;

import com.starlinkiraq.store.config.AppProperties;
import com.starlinkiraq.store.dto.product.ProductRequest;
import com.starlinkiraq.store.dto.product.ProductResponse;
import com.starlinkiraq.store.dto.product.ProductVariantResponse;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.ProductType;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.DigitalAssetRepository;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * اختبارات وحدة لخدمة المنتجات: الإنشاء، التعديل، الحذف (إلغاء التفعيل)، وحالة المخزون المنخفض.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private DigitalAssetRepository digitalAssetRepository;
    @Mock
    private EmailService emailService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.setLowStockThreshold(5);
        productService = new ProductService(productRepository, wishlistRepository, digitalAssetRepository,
                emailService, appProperties);
    }

    private ProductRequest buildRequest() {
        return new ProductRequest("طبق Starlink", "وصف المنتج", BigDecimal.valueOf(900), null,
                "https://example.com/dish.png", ProductType.PHYSICAL, 10, true, null, "dishes", true, null, null);
    }

    @Test
    void createProduct_shouldPersistAndReturnResponse() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = productService.createProduct(buildRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("طبق Starlink");
        assertThat(response.lowStock()).isFalse();
    }

    @Test
    void getProduct_shouldThrowNotFound_whenProductMissing() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProduct_shouldMarkLowStock_whenStockAtOrBelowThreshold() {
        Product product = Product.builder()
                .id(1L).name("راوتر Starlink").price(BigDecimal.valueOf(400))
                .productType(ProductType.PHYSICAL).requiresShipping(true).stockQuantity(3)
                .category("routers").isActive(true).averageRating(BigDecimal.ZERO)
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.lowStock()).isTrue();
    }

    @Test
    void deleteProduct_shouldDeactivateInsteadOfHardDelete() {
        Product product = Product.builder().id(1L).name("منتج").price(BigDecimal.TEN)
                .productType(ProductType.PHYSICAL).requiresShipping(true).isActive(true)
                .category("misc").averageRating(BigDecimal.ZERO).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.deleteProduct(1L);

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void getVariants_shouldReturnEmptyList_whenProductHasNoVariantGroup() {
        Product product = Product.builder().id(1L).name("منتج فريد").price(BigDecimal.TEN)
                .productType(ProductType.PHYSICAL).requiresShipping(true).isActive(true)
                .category("misc").averageRating(BigDecimal.ZERO).variantGroupKey(null).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductVariantResponse> variants = productService.getVariants(1L);

        assertThat(variants).isEmpty();
    }

    @Test
    void getVariants_shouldReturnAllModelsInSameGroup_whenProductHasVariantGroup() {
        Product mini = Product.builder().id(1L).name("طبق Starlink Mini").price(BigDecimal.valueOf(500))
                .productType(ProductType.PHYSICAL).requiresShipping(true).isActive(true)
                .category("dishes").averageRating(BigDecimal.ZERO)
                .variantGroupKey("starlink-dish").variantLabel("Mini").build();
        Product standard = Product.builder().id(2L).name("طبق Starlink Standard").price(BigDecimal.valueOf(900))
                .productType(ProductType.PHYSICAL).requiresShipping(true).isActive(true)
                .category("dishes").averageRating(BigDecimal.ZERO)
                .variantGroupKey("starlink-dish").variantLabel("Standard").build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(mini));
        when(productRepository.findByVariantGroupKeyAndIsActiveTrueOrderByVariantLabelAsc("starlink-dish"))
                .thenReturn(List.of(mini, standard));

        List<ProductVariantResponse> variants = productService.getVariants(1L);

        assertThat(variants).hasSize(2);
        assertThat(variants).extracting(ProductVariantResponse::variantLabel).containsExactly("Mini", "Standard");
    }
}
