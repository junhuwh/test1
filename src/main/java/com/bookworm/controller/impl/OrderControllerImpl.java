package com.bookworm.controller.impl;

import com.bookworm.controller.OrderController;
import com.bookworm.dto.request.CreateOrderRequest;
import com.bookworm.dto.response.BuyAgainResponse;
import com.bookworm.dto.response.CancelOrderResponse;
import com.bookworm.dto.response.OrderDto;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;

    public OrderControllerImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public ResponseEntity<OrderDto> createOrder(CreateOrderRequest request) {
        UUID userId = getCurrentUserId();
        OrderDto order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Override
    public ResponseEntity<List<OrderDto>> getOrderHistory() {
        UUID userId = getCurrentUserId();
        List<OrderDto> history = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Override
    public ResponseEntity<CancelOrderResponse> cancelOrder(UUID id) {
        UUID userId = getCurrentUserId();
        CancelOrderResponse response = orderService.cancelOrder(userId, id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BuyAgainResponse> buyAgainOrder(UUID id) {
        UUID userId = getCurrentUserId();
        BuyAgainResponse response = orderService.buyAgain(userId, id);
        return ResponseEntity.ok(response);
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
