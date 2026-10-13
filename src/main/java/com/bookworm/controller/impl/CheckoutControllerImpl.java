package com.bookworm.controller.impl;

import com.bookworm.controller.CheckoutController;
import com.bookworm.dto.request.CheckoutQuoteRequest;
import com.bookworm.dto.response.CheckoutQuoteResponse;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CheckoutControllerImpl implements CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutControllerImpl(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Override
    public ResponseEntity<CheckoutQuoteResponse> calculateCheckoutQuote(CheckoutQuoteRequest request) {
        UUID userId = getCurrentUserId();
        CheckoutQuoteResponse quote = checkoutService.calculateQuote(userId, request);
        return ResponseEntity.ok(quote);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID in security context: " + auth.getName());
        }
    }
}
