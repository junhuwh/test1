package com.bookworm.service;

import com.bookworm.dto.request.AddToCartRequest;
import com.bookworm.dto.request.UpdateCartItemRequest;
import com.bookworm.dto.response.CartDto;

import java.util.UUID;

public interface CartService {
    CartDto getCart(UUID currentUserId);
    CartDto addItemToCart(UUID currentUserId, AddToCartRequest request);
    CartDto updateCartItemQuantity(UUID currentUserId, UUID cartItemId, UpdateCartItemRequest request);
}
