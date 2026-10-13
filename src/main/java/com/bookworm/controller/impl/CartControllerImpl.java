package com.bookworm.controller.impl;

import com.bookworm.controller.CartController;
import com.bookworm.dto.request.AddToCartRequest;
import com.bookworm.dto.request.UpdateCartItemRequest;
import com.bookworm.dto.response.CartDto;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CartControllerImpl implements CartController {

    private final CartService cartService;

    public CartControllerImpl(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public ResponseEntity<CartDto> getCart() {
        UUID userId = getCurrentUserId();
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @Override
    public ResponseEntity<CartDto> addItemToCart(AddToCartRequest request) {
        UUID userId = getCurrentUserId();
        CartDto cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    @Override
    public ResponseEntity<CartDto> updateCartItemQuantity(UUID id, UpdateCartItemRequest request) {
        UUID userId = getCurrentUserId();
        CartDto cart = cartService.updateCartItemQuantity(userId, id, request);
        return ResponseEntity.ok(cart);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID in security token: " + auth.getName());
        }
    }
}
