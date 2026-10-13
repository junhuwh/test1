package com.bookworm.controller;

import com.bookworm.dto.request.CreateReviewRequest;
import com.bookworm.dto.response.ReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Reviews", description = "Customer book ratings and reviews")
@RequestMapping("/api/v1/books")
public interface ReviewController {

    @Operation(summary = "Submit book review and rating", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ReviewDto> createReview(
        @PathVariable UUID id,
        @Valid @RequestBody CreateReviewRequest request
    );
}
