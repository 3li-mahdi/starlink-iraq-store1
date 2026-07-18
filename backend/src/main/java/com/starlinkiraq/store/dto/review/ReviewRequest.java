package com.starlinkiraq.store.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @Min(value = 1, message = "التقييم يجب أن يكون بين 1 و5")
        @Max(value = 5, message = "التقييم يجب أن يكون بين 1 و5")
        int rating,

        @Size(max = 2000, message = "التعليق طويل جداً")
        String comment
) {
}
