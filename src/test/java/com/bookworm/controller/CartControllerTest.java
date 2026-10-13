package com.bookworm.controller;

import com.bookworm.controller.impl.CartControllerImpl;
import com.bookworm.dto.request.AddToCartRequest;
import com.bookworm.dto.request.UpdateCartItemRequest;
import com.bookworm.dto.response.CartDto;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartControllerImpl cartController;

    private UUID userId;
    private CartDto mockCartDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockCartDto = new CartDto();
        mockCartDto.setCartId(UUID.randomUUID());
        mockCartDto.setTotalItems(1);
        mockCartDto.setSubtotal(new BigDecimal("149.00"));
        mockCartDto.setItems(new ArrayList<>());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userId.toString(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/v1/cart returns 200 OK and CartDto for authenticated user")
    void testGetCart_Success() {
        when(cartService.getCart(userId)).thenReturn(mockCartDto);

        ResponseEntity<CartDto> response = cartController.getCart();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSubtotal()).isEqualByComparingTo(new BigDecimal("149.00"));
        verify(cartService, times(1)).getCart(userId);
    }

    @Test
    @DisplayName("POST /api/v1/cart/items returns 200 OK after adding item")
    void testAddItemToCart_Success() {
        AddToCartRequest request = new AddToCartRequest();
        request.setBookId(UUID.randomUUID());
        request.setQuantity(2);

        when(cartService.addItemToCart(userId, request)).thenReturn(mockCartDto);

        ResponseEntity<CartDto> response = cartController.addItemToCart(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cartService, times(1)).addItemToCart(userId, request);
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{id} returns 200 OK after updating quantity")
    void testUpdateCartItemQuantity_Success() {
        UUID itemId = UUID.randomUUID();
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartService.updateCartItemQuantity(userId, itemId, request)).thenReturn(mockCartDto);

        ResponseEntity<CartDto> response = cartController.updateCartItemQuantity(itemId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cartService, times(1)).updateCartItemQuantity(userId, itemId, request);
    }

    @Test
    @DisplayName("GET /api/v1/cart throws UnauthorizedException when authentication is absent")
    void testGetCart_Unauthenticated_ThrowsException() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, () -> cartController.getCart());
        verify(cartService, never()).getCart(any());
    }
}
