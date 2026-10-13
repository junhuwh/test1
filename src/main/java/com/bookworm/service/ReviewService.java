package com.bookworm.service;

import com.bookworm.dto.request.CreateReviewRequest;
import com.bookworm.dto.response.ReviewDto;

import java.util.UUID;

public interface ReviewService {
    ReviewDto createReview(UUID bookId, CreateReviewRequest request, UUID currentUserId);
}
