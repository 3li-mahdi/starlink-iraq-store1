package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.review.ReviewRequest;
import com.starlinkiraq.store.dto.review.ReviewResponse;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.entity.Review;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.exception.ConflictException;
import com.starlinkiraq.store.exception.ResourceNotFoundException;
import com.starlinkiraq.store.repository.ProductRepository;
import com.starlinkiraq.store.repository.ReviewRepository;
import com.starlinkiraq.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * يدير مراجعات وتقييمات المنتجات: الإضافة، العرض العام (المعتمد فقط)، والاعتماد الإداري.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * يجلب المراجعات المعتمدة فقط لمنتج معيّن مع الصفحات.
     *
     * @param productId معرّف المنتج
     * @param pageable معلومات الصفحة والحجم
     * @return صفحة من المراجعات المعتمدة
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getApprovedReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProduct_IdAndIsApprovedTrue(productId, pageable).map(this::toResponse);
    }

    /**
     * يضيف مراجعة جديدة لمنتج من مستخدم مسجَّل دخوله. تبقى المراجعة غير ظاهرة للعامة حتى يعتمدها الأدمن.
     *
     * @param userId معرّف المستخدم كاتب المراجعة
     * @param productId معرّف المنتج
     * @param request التقييم والتعليق
     * @return بيانات المراجعة بعد الإنشاء (بانتظار الاعتماد)
     * تأثير جانبي: يضيف سجلاً جديداً بجدول reviews
     */
    @Transactional
    public ReviewResponse submitReview(Long userId, Long productId, ReviewRequest request) {
        if (reviewRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ConflictException("لقد قمت بمراجعة هذا المنتج مسبقاً");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("المنتج غير موجود"));
        User user = userRepository.getReferenceById(userId);

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .isApproved(false)
                .build();
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    /**
     * يعتمد مراجعة معلَّقة ليصبح ظاهرة للعامة، ويعيد احتساب متوسط تقييم المنتج (للأدمن فقط).
     *
     * @param reviewId معرّف المراجعة
     * تأثير جانبي: يعدّل isApproved للمراجعة، ويعدّل averageRating للمنتج المرتبط
     */
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("المراجعة غير موجودة"));
        review.setApproved(true);
        reviewRepository.save(review);
        recalculateAverageRating(review.getProduct());
    }

    private void recalculateAverageRating(Product product) {
        List<Review> approvedReviews = reviewRepository
                .findByProduct_IdAndIsApprovedTrue(product.getId(), Pageable.unpaged())
                .getContent();

        BigDecimal average = approvedReviews.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(approvedReviews.stream().mapToInt(Review::getRating).average().orElse(0))
                        .setScale(2, RoundingMode.HALF_UP);

        product.setAverageRating(average);
        productRepository.save(product);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getProduct().getId(), review.getUser().getFullName(),
                review.getRating(), review.getComment(), review.isApproved(), review.getCreatedAt());
    }
}
