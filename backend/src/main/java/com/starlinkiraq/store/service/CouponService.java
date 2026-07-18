package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.coupon.CouponRequest;
import com.starlinkiraq.store.dto.coupon.CouponResponse;
import com.starlinkiraq.store.dto.coupon.CouponValidateResponse;
import com.starlinkiraq.store.entity.Coupon;
import com.starlinkiraq.store.entity.DiscountType;
import com.starlinkiraq.store.exception.ConflictException;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * يدير كوبونات الخصم: التحقق من صلاحيتها وتطبيقها على مبلغ الطلب، وعمليات الإدارة (للأدمن).
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * يتحقق من صلاحية كوبون خصم لمبلغ طلب معيّن ويحسب قيمة الخصم المستحقة.
     *
     * @param code كود الكوبون
     * @param orderAmount المبلغ الإجمالي للطلب قبل الخصم
     * @return نتيجة التحقق مع قيمة الخصم إذا كان الكوبون صالحاً
     */
    @Transactional(readOnly = true)
    public CouponValidateResponse validateCoupon(String code, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCode(code).orElse(null);

        if (coupon == null || !coupon.isCurrentlyValid()) {
            return new CouponValidateResponse(false, "كود الخصم غير صالح أو منتهي الصلاحية", BigDecimal.ZERO);
        }
        if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return new CouponValidateResponse(false,
                    "الحد الأدنى لاستخدام هذا الكوبون هو " + coupon.getMinOrderAmount(), BigDecimal.ZERO);
        }

        BigDecimal discount = calculateDiscount(coupon, orderAmount);
        return new CouponValidateResponse(true, "تم تطبيق الخصم بنجاح", discount);
    }

    /**
     * يحسب مبلغ الخصم الفعلي بناءً على نوع الكوبون (نسبة أو مبلغ ثابت)، بدون تجاوز مبلغ الطلب نفسه.
     *
     * @param coupon الكوبون المطلوب حساب خصمه
     * @param orderAmount المبلغ الإجمالي للطلب قبل الخصم
     * @return قيمة الخصم النهائية
     */
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();
        return discount.min(orderAmount);
    }

    /**
     * يزيد عداد استخدام الكوبون بواحد بعد استخدامه بنجاح في طلب.
     *
     * @param code كود الكوبون
     * تأثير جانبي: يعدّل currentUses للكوبون بقاعدة البيانات
     */
    @Transactional
    public void incrementUsage(String code) {
        couponRepository.findByCode(code).ifPresent(coupon -> {
            coupon.setCurrentUses(coupon.getCurrentUses() + 1);
            couponRepository.save(coupon);
        });
    }

    /**
     * يُنشئ كوبون خصم جديداً (للأدمن فقط).
     *
     * @param request بيانات الكوبون
     * @return بيانات الكوبون بعد الإنشاء
     * تأثير جانبي: يضيف سجلاً جديداً بجدول coupons
     */
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.findByCode(request.code()).isPresent()) {
            throw new ConflictException("كود الكوبون مستخدم مسبقاً");
        }
        Coupon coupon = Coupon.builder()
                .code(request.code())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderAmount(request.minOrderAmount() != null ? request.minOrderAmount() : BigDecimal.ZERO)
                .maxUses(request.maxUses())
                .currentUses(0)
                .expiresAt(request.expiresAt())
                .isActive(request.isActive())
                .build();
        coupon = couponRepository.save(coupon);
        return CouponResponse.from(coupon);
    }

    /**
     * يجلب كل الكوبونات (للأدمن فقط).
     *
     * @return قائمة كل الكوبونات
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream().map(CouponResponse::from).toList();
    }

    /**
     * يفعّل أو يعطّل كوبوناً موجوداً (للأدمن فقط).
     *
     * @param id معرّف الكوبون
     * @param isActive الحالة الجديدة (مفعّل/معطّل)
     * @return بيانات الكوبون بعد التعديل
     * تأثير جانبي: يعدّل isActive للكوبون بقاعدة البيانات
     */
    @Transactional
    public CouponResponse setCouponActive(Long id, boolean isActive) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الكوبون غير موجود"));
        coupon.setActive(isActive);
        coupon = couponRepository.save(coupon);
        return CouponResponse.from(coupon);
    }
}
