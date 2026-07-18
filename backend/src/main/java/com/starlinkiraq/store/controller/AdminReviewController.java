package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * اعتماد مراجعات المنتجات المعلَّقة قبل ظهورها للعامة، متاح للأدمن فقط.
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Reviews", description = "اعتماد مراجعات المنتجات (للأدمن فقط)")
public class AdminReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "اعتماد مراجعة معلَّقة لتصبح ظاهرة للعامة")
    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approveReview(@PathVariable Long id) {
        reviewService.approveReview(id);
        return ResponseEntity.noContent().build();
    }
}
