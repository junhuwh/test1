package com.bookworm.service;

import com.bookworm.dto.request.CreateOrderRequest;
import com.bookworm.dto.response.BuyAgainResponse;
import com.bookworm.dto.response.CancelOrderResponse;
import com.bookworm.dto.response.OrderDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(UUID currentUserId, CreateOrderRequest request);
    List<OrderDto> getOrderHistory(UUID currentUserId);
    CancelOrderResponse cancelOrder(UUID currentUserId, UUID orderId);
    BuyAgainResponse buyAgain(UUID currentUserId, UUID orderId);
}
