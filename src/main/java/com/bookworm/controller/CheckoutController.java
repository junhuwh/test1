package com.bookworm.controller;

import com.bookworm.dto.request.CheckoutQuoteRequest;
import com.bookworm.dto.response.CheckoutQuoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Checkout", description = "Checkout price calculation and quote generation")
@RequestMapping("/api/v1/checkout")
public interface CheckoutController {

    @Operation(summary = "Calculate checkout totals and quote", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/quote")
    ResponseEntity<CheckoutQuoteResponse> calculateCheckoutQuote(
        @RequestBody(required = false) CheckoutQuoteRequest request
    );
}
