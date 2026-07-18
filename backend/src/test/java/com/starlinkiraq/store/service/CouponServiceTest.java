package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.coupon.CouponValidateResponse;
import com.starlinkiraq.store.entity.Coupon;
import com.starlinkiraq.store.entity.DiscountType;
import com.starlinkiraq.store.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * اختبارات وحدة لخدمة الكوبونات: التحقق من الصلاحية وحساب الخصم.
 */
@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository);
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenCouponDoesNotExist() {
        when(couponRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        CouponValidateResponse response = couponService.validateCoupon("MISSING", BigDecimal.valueOf(100));

        assertThat(response.valid()).isFalse();
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenExpired() {
        Coupon coupon = Coupon.builder()
                .code("EXPIRED10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .minOrderAmount(BigDecimal.ZERO)
                .isActive(true)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(couponRepository.findByCode("EXPIRED10")).thenReturn(Optional.of(coupon));

        CouponValidateResponse response = couponService.validateCoupon("EXPIRED10", BigDecimal.valueOf(100));

        assertThat(response.valid()).isFalse();
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenBelowMinOrderAmount() {
        Coupon coupon = Coupon.builder()
                .code("MIN500")
                .discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(50))
                .minOrderAmount(BigDecimal.valueOf(500))
                .isActive(true)
                .build();
        when(couponRepository.findByCode("MIN500")).thenReturn(Optional.of(coupon));

        CouponValidateResponse response = couponService.validateCoupon("MIN500", BigDecimal.valueOf(100));

        assertThat(response.valid()).isFalse();
    }

    @Test
    void validateCoupon_shouldCalculatePercentageDiscount_whenValid() {
        Coupon coupon = Coupon.builder()
                .code("SAVE10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .minOrderAmount(BigDecimal.ZERO)
                .isActive(true)
                .build();
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidateResponse response = couponService.validateCoupon("SAVE10", BigDecimal.valueOf(1000));

        assertThat(response.valid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2));
    }

    @Test
    void validateCoupon_shouldNotExceedOrderAmount_whenFixedDiscountIsLarger() {
        Coupon coupon = Coupon.builder()
                .code("BIG100")
                .discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(1000))
                .minOrderAmount(BigDecimal.ZERO)
                .isActive(true)
                .build();
        when(couponRepository.findByCode("BIG100")).thenReturn(Optional.of(coupon));

        CouponValidateResponse response = couponService.validateCoupon("BIG100", BigDecimal.valueOf(100));

        assertThat(response.valid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenMaxUsesReached() {
        Coupon coupon = Coupon.builder()
                .code("LIMITED")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN)
                .minOrderAmount(BigDecimal.ZERO)
                .maxUses(5)
                .currentUses(5)
                .isActive(true)
                .build();
        when(couponRepository.findByCode("LIMITED")).thenReturn(Optional.of(coupon));

        CouponValidateResponse response = couponService.validateCoupon("LIMITED", BigDecimal.valueOf(100));

        assertThat(response.valid()).isFalse();
    }
}
