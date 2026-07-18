package com.starlinkiraq.store.dto.product;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * طلب إداري لإضافة نسخ جديدة من المحتوى الرقمي (أكواد تفعيل/روابط) لمنتج رقمي معيّن.
 */
public record AddDigitalAssetsRequest(
        @NotEmpty(message = "يجب إضافة محتوى رقمي واحد على الأقل")
        List<@jakarta.validation.constraints.NotBlank String> contents
) {
}
