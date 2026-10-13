package com.bookworm.controller;

import com.bookworm.dto.request.AddToCartRequest;
import com.bookworm.dto.request.UpdateCartItemRequest;
import com.bookworm.dto.response.CartDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart and item operations")
@RequestMapping("/api/v1/cart")
public interface CartController {

    @Operation(summary = "Get user shopping cart", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    ResponseEntity<CartDto> getCart();

    @Operation(summary = "Add book to shopping cart", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/items")
    ResponseEntity<CartDto> addItemToCart(@Valid @RequestBody AddToCartRequest request);

    @Operation(summary = "Update cart item quantity", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/items/{id}")
    ResponseEntity<CartDto> updateCartItemQuantity(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateCartItemRequest request
    );
}
