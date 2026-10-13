package com.bookworm.controller;

import com.bookworm.dto.request.CreateOrderRequest;
import com.bookworm.dto.response.BuyAgainResponse;
import com.bookworm.dto.response.CancelOrderResponse;
import com.bookworm.dto.response.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders", description = "Order placement, order history, cancellation, and re-order")
@RequestMapping("/api/v1/orders")
public interface OrderController {

    @Operation(summary = "Create and place order", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request);

    @Operation(summary = "Get user order history", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    ResponseEntity<List<OrderDto>> getOrderHistory();

    @Operation(summary = "Cancel order within 48 hours", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/cancel")
    ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable UUID id);

    @Operation(summary = "Re-add order items to cart", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/buy-again")
    ResponseEntity<BuyAgainResponse> buyAgainOrder(@PathVariable UUID id);
}
