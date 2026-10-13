package com.bookworm.controller;

import com.bookworm.controller.impl.OrderControllerImpl;
import com.bookworm.dto.request.CreateOrderRequest;
import com.bookworm.dto.response.BuyAgainResponse;
import com.bookworm.dto.response.CancelOrderResponse;
import com.bookworm.dto.response.OrderDto;
import com.bookworm.entity.enums.OrderStatus;
import com.bookworm.service.OrderService;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderControllerImpl orderController;

    private UUID userId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userId.toString(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/v1/orders returns 201 Created and OrderDto")
    void testCreateOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();

        OrderDto mockOrder = new OrderDto();
        mockOrder.setId(orderId);
        mockOrder.setOrderNumber("BW-12345");
        mockOrder.setStatus(OrderStatus.PAID);

        when(orderService.createOrder(userId, request)).thenReturn(mockOrder);

        ResponseEntity<OrderDto> response = orderController.createOrder(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderNumber()).isEqualTo("BW-12345");
        verify(orderService, times(1)).createOrder(userId, request);
    }

    @Test
    @DisplayName("GET /api/v1/orders returns 200 OK and order history")
    void testGetOrderHistory_Success() {
        OrderDto mockOrder = new OrderDto();
        mockOrder.setOrderNumber("BW-12345");

        when(orderService.getOrderHistory(userId)).thenReturn(List.of(mockOrder));

        ResponseEntity<List<OrderDto>> response = orderController.getOrderHistory();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(orderService, times(1)).getOrderHistory(userId);
    }

    @Test
    @DisplayName("POST /api/v1/orders/{id}/cancel returns 200 OK and CancelOrderResponse")
    void testCancelOrder_Success() {
        CancelOrderResponse mockResponse = new CancelOrderResponse();
        mockResponse.setOrderId(orderId);
        mockResponse.setStatus("CANCELLED");
        mockResponse.setRefundStatus("PROCESSED");
        mockResponse.setRefundAmount(new BigDecimal("216.88"));

        when(orderService.cancelOrder(userId, orderId)).thenReturn(mockResponse);

        ResponseEntity<CancelOrderResponse> response = orderController.cancelOrder(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("CANCELLED");
        verify(orderService, times(1)).cancelOrder(userId, orderId);
    }

    @Test
    @DisplayName("POST /api/v1/orders/{id}/buy-again returns 200 OK and BuyAgainResponse")
    void testBuyAgainOrder_Success() {
        BuyAgainResponse mockResponse = new BuyAgainResponse();
        mockResponse.setOrderId(orderId);
        mockResponse.setItemsAddedCount(2);

        when(orderService.buyAgain(userId, orderId)).thenReturn(mockResponse);

        ResponseEntity<BuyAgainResponse> response = orderController.buyAgainOrder(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItemsAddedCount()).isEqualTo(2);
        verify(orderService, times(1)).buyAgain(userId, orderId);
    }
}
