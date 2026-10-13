package com.bookworm.service;

import com.bookworm.dto.request.CreateOrderRequest;
import com.bookworm.dto.request.OrderAddressDto;
import com.bookworm.dto.response.BuyAgainResponse;
import com.bookworm.dto.response.CancelOrderResponse;
import com.bookworm.dto.response.OrderDto;
import com.bookworm.entity.*;
import com.bookworm.entity.enums.BookFormat;
import com.bookworm.entity.enums.OrderStatus;
import com.bookworm.entity.enums.PaymentMethod;
import com.bookworm.exception.BusinessRuleViolationException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.*;
import com.bookworm.service.impl.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private OrderServiceImpl orderService;

    private UUID userId;
    private User testUser;
    private Cart testCart;
    private Book testBook;
    private Order testOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        objectMapper = new ObjectMapper();
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, cartRepository, cartItemRepository, userRepository, objectMapper);

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("charlie@example.com");

        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        testBook = new Book();
        testBook.setId(UUID.randomUUID());
        testBook.setTitle("Joy of Minimalism");
        testBook.setPrice(new BigDecimal("149.00"));
        testBook.setFormat(BookFormat.Paperback);

        testOrder = new Order();
        testOrder.setId(orderId);
        testOrder.setOrderNumber("BW-1791844635976");
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PAID);
        testOrder.setSubtotal(new BigDecimal("149.00"));
        testOrder.setTax(new BigDecimal("17.88"));
        testOrder.setShippingFee(new BigDecimal("50.00"));
        testOrder.setDiscount(BigDecimal.ZERO);
        testOrder.setTotalAmount(new BigDecimal("216.88"));
        testOrder.setOrderDate(Instant.now());
        testOrder.setCancelEligibleUntil(Instant.now().plus(48, ChronoUnit.HOURS));
        testOrder.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("createOrder should calculate totals, persist order and items, and clear cart")
    void testCreateOrder_Success() {
        CartItem item = new CartItem();
        item.setBook(testBook);
        item.setQuantity(2);
        testCart.getItems().add(item);

        OrderAddressDto address = new OrderAddressDto();
        address.setFirstName("Charlie");
        address.setLastName("Brown");
        address.setAddressLine("123 Street");
        address.setCity("Mumbai");
        address.setState("Maharashtra");
        address.setPostalCode("400001");
        address.setPhone("+91 9876543210");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress(address);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setCouponCode("SAVE100");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
            OrderItem oi = invocation.getArgument(0);
            oi.setId(UUID.randomUUID());
            return oi;
        });

        OrderDto orderDto = orderService.createOrder(userId, request);

        assertThat(orderDto).isNotNull();
        assertThat(orderDto.getOrderNumber()).startsWith("BW-");
        assertThat(orderDto.getTrackingNumber()).startsWith("BW-TRK-");
        assertThat(orderDto.getEstimatedDelivery()).isEqualTo(LocalDate.now().plusDays(5));
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(orderDto.getItems()).hasSize(1);
        assertThat(orderDto.getItems().get(0).getTitle()).isEqualTo("Joy of Minimalism");

        verify(cartItemRepository, times(1)).deleteByCartId(testCart.getId());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    @DisplayName("createOrder should throw BusinessRuleViolationException when cart is empty")
    void testCreateOrder_EmptyCart_ThrowsException() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        assertThrows(BusinessRuleViolationException.class, () -> orderService.createOrder(userId, request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrderHistory should return user orders")
    void testGetOrderHistory_Success() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(userId)).thenReturn(List.of(testOrder));

        List<OrderDto> history = orderService.getOrderHistory(userId);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getOrderNumber()).isEqualTo("BW-1791844635976");
        verify(orderRepository, times(1)).findByUserIdOrderByOrderDateDesc(userId);
    }

    @Test
    @DisplayName("cancelOrder within 48 hours should update status to CANCELLED and return CancelOrderResponse")
    void testCancelOrder_Within48Hours_Success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        CancelOrderResponse response = orderService.cancelOrder(userId, orderId);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(response.getRefundStatus()).isEqualTo("PROCESSED");
        assertThat(response.getRefundAmount()).isEqualByComparingTo(new BigDecimal("216.88"));
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("cancelOrder should throw BusinessRuleViolationException when 48-hour window has expired")
    void testCancelOrder_ExpiredPeriod_ThrowsException() {
        // Set eligible date to 1 hour in the past
        testOrder.setCancelEligibleUntil(Instant.now().minus(1, ChronoUnit.HOURS));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
            () -> orderService.cancelOrder(userId, orderId));

        assertThat(ex.getMessage()).contains("48 hours");
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelOrder should throw BusinessRuleViolationException when order is already cancelled")
    void testCancelOrder_AlreadyCancelled_ThrowsException() {
        testOrder.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
            () -> orderService.cancelOrder(userId, orderId));

        assertThat(ex.getMessage()).contains("already cancelled");
    }

    @Test
    @DisplayName("cancelOrder should throw ResourceNotFoundException when order belongs to different user")
    void testCancelOrder_DifferentUser_ThrowsException() {
        UUID otherUserId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(otherUserId, orderId));
    }

    @Test
    @DisplayName("buyAgain should re-populate cart with historical order items and return BuyAgainResponse")
    void testBuyAgain_Success_RepopulatesCart() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setOrder(testOrder);
        orderItem.setBook(testBook);
        orderItem.setTitle("Joy of Minimalism");
        orderItem.setPrice(new BigDecimal("149.00"));
        orderItem.setQuantity(2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndBookId(testCart.getId(), testBook.getId())).thenReturn(Optional.empty());

        BuyAgainResponse response = orderService.buyAgain(userId, orderId);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getItemsAddedCount()).isEqualTo(2);
        assertThat(response.getMessage()).contains("2 items");

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    @DisplayName("buyAgain should accumulate quantity when book is already present in cart")
    void testBuyAgain_ExistingItem_AccumulatesQuantity() {
        CartItem existingCartItem = new CartItem();
        existingCartItem.setId(UUID.randomUUID());
        existingCartItem.setCart(testCart);
        existingCartItem.setBook(testBook);
        existingCartItem.setQuantity(1);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        orderItem.setBook(testBook);
        orderItem.setQuantity(3);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndBookId(testCart.getId(), testBook.getId())).thenReturn(Optional.of(existingCartItem));

        BuyAgainResponse response = orderService.buyAgain(userId, orderId);

        assertThat(response).isNotNull();
        assertThat(existingCartItem.getQuantity()).isEqualTo(4); // 1 + 3 = 4
        verify(cartItemRepository, times(1)).save(existingCartItem);
    }
}
