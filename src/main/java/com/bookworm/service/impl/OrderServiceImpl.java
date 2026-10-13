package com.bookworm.service.impl;

import com.bookworm.dto.request.CreateOrderRequest;
import com.bookworm.dto.request.OrderAddressDto;
import com.bookworm.dto.response.*;
import com.bookworm.entity.*;
import com.bookworm.entity.enums.BookFormat;
import com.bookworm.entity.enums.OrderStatus;
import com.bookworm.exception.BusinessRuleViolationException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.*;
import com.bookworm.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public OrderServiceImpl(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public OrderDto createOrder(UUID currentUserId, CreateOrderRequest request) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        Cart cart = cartRepository.findByUserId(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + currentUserId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessRuleViolationException("Cannot place order with an empty cart");
        }

        // Dynamically compute subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal price = item.getBook().getPrice() != null ? item.getBook().getPrice() : BigDecimal.ZERO;
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // Tax = subtotal * 0.12
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);

        // Shipping = 0 if subtotal >= 499 else 50
        BigDecimal shippingFee = BigDecimal.ZERO.setScale(2);
        if (subtotal.compareTo(new BigDecimal("499.00")) < 0) {
            shippingFee = new BigDecimal("50.00").setScale(2);
        }

        // Coupon SAVE100 gives 100 discount
        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        if (request.getCouponCode() != null && "SAVE100".equalsIgnoreCase(request.getCouponCode().trim())) {
            discount = new BigDecimal("100.00").min(subtotal).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subtotal
            .add(tax)
            .add(shippingFee)
            .subtract(discount)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

        // Generate identifiers
        String orderNumber = "BW-" + System.currentTimeMillis();
        String trackingNumber = "BW-TRK-" + (100000 + new Random().nextInt(900000));
        LocalDate estimatedDelivery = LocalDate.now().plusDays(5);
        Instant orderDate = Instant.now();
        Instant cancelEligibleUntil = orderDate.plus(48, ChronoUnit.HOURS);

        String shippingAddressJson;
        try {
            shippingAddressJson = objectMapper.writeValueAsString(request.getShippingAddress());
        } catch (JsonProcessingException e) {
            throw new BusinessRuleViolationException("Failed to serialize shipping address: " + e.getMessage());
        }

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShippingFee(shippingFee);
        order.setDiscount(discount);
        order.setTotalAmount(totalAmount);
        order.setShippingAddressJson(shippingAddressJson);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTrackingNumber(trackingNumber);
        order.setEstimatedDelivery(estimatedDelivery);
        order.setOrderDate(orderDate);
        order.setCancelEligibleUntil(cancelEligibleUntil);

        Order savedOrder = orderRepository.save(order);

        // Persist OrderItems snapshot
        List<OrderItem> savedOrderItems = new ArrayList<>();
        List<OrderItemDto> itemDtos = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setBook(cartItem.getBook());
            orderItem.setTitle(cartItem.getBook().getTitle());
            orderItem.setPrice(cartItem.getBook().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());

            OrderItem savedItem = orderItemRepository.save(orderItem);
            savedOrderItems.add(savedItem);

            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setBookId(cartItem.getBook().getId());
            itemDto.setTitle(cartItem.getBook().getTitle());
            itemDto.setPrice(cartItem.getBook().getPrice());
            itemDto.setQuantity(cartItem.getQuantity());
            itemDtos.add(itemDto);
        }
        savedOrder.setItems(savedOrderItems);

        // Clear user cart after successful order creation
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return toOrderDto(savedOrder, request.getShippingAddress(), itemDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrderHistory(UUID currentUserId) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(currentUserId);
        return orders.stream().map(order -> {
            OrderAddressDto addressDto = null;
            try {
                if (order.getShippingAddressJson() != null) {
                    addressDto = objectMapper.readValue(order.getShippingAddressJson(), OrderAddressDto.class);
                }
            } catch (JsonProcessingException ignored) {}

            List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
                OrderItemDto dto = new OrderItemDto();
                if (item.getBook() != null) {
                    dto.setBookId(item.getBook().getId());
                }
                dto.setTitle(item.getTitle());
                dto.setPrice(item.getPrice());
                dto.setQuantity(item.getQuantity());
                return dto;
            }).toList();

            return toOrderDto(order, addressDto, itemDtos);
        }).toList();
    }

    @Override
    public CancelOrderResponse cancelOrder(UUID currentUserId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // 1. Verify order belongs to authenticated user
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        // 2. Verify status is not already CANCELLED
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Order is already cancelled");
        }

        // 3. Verify current time is before cancelEligibleUntil (48 hours window)
        if (Instant.now().isAfter(order.getCancelEligibleUntil())) {
            throw new BusinessRuleViolationException("Order cancellation window (48 hours) has expired");
        }

        // 4. Update order status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 5. Return CancelOrderResponse
        CancelOrderResponse response = new CancelOrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(OrderStatus.CANCELLED.name());
        response.setRefundAmount(order.getTotalAmount());
        response.setRefundStatus("PROCESSED");
        response.setMessage("Order successfully cancelled within 48 hours. Refund of INR " + order.getTotalAmount() + " processed.");

        return response;
    }

    @Override
    public BuyAgainResponse buyAgain(UUID currentUserId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        // 1. Read OrderItems from historical order
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            orderItems = order.getItems();
        }

        // 2. Re-populate active cart
        Cart cart = cartRepository.findByUserId(currentUserId).orElseGet(() -> {
            User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));
            Cart c = new Cart();
            c.setUser(user);
            c.setUpdatedAt(Instant.now());
            return cartRepository.save(c);
        });

        int itemsAddedCount = 0;
        if (orderItems != null) {
            for (OrderItem orderItem : orderItems) {
                Book book = orderItem.getBook();
                if (book == null) continue;

                int qtyToAdd = orderItem.getQuantity() != null ? orderItem.getQuantity() : 1;

                // 3. If book already exists in cart, accumulate quantity
                Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId());
                if (existingCartItem.isPresent()) {
                    CartItem item = existingCartItem.get();
                    item.setQuantity(item.getQuantity() + qtyToAdd);
                    cartItemRepository.save(item);
                } else {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setBook(book);
                    newItem.setQuantity(qtyToAdd);
                    cartItemRepository.save(newItem);
                }
                itemsAddedCount += qtyToAdd;
            }
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        // Fetch refreshed cart with all items
        Cart updatedCart = cartRepository.findByUserId(currentUserId).orElse(cart);

        // 4. Return BuyAgainResponse
        BuyAgainResponse response = new BuyAgainResponse();
        response.setOrderId(order.getId());
        response.setItemsAddedCount(itemsAddedCount);
        response.setCart(toCartDto(updatedCart));
        response.setMessage(itemsAddedCount + " items from order " + order.getOrderNumber() + " added to your cart.");

        return response;
    }

    private CartDto toCartDto(Cart cart) {
        List<CartItemDto> itemDtos = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Book book = item.getBook();
                BigDecimal unitPrice = book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO;
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

                CartItemDto itemDto = new CartItemDto();
                itemDto.setItemId(item.getId());
                itemDto.setBookId(book.getId());
                itemDto.setTitle(book.getTitle());
                itemDto.setAuthorName(book.getAuthorName());
                itemDto.setFormat(book.getFormat() != null ? book.getFormat().name() : BookFormat.Paperback.name());
                itemDto.setUnitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP));
                itemDto.setQuantity(qty);
                itemDto.setTotalPrice(lineTotal);
                itemDto.setFrontCoverUrl(book.getFrontCoverUrl());

                itemDtos.add(itemDto);

                subtotal = subtotal.add(lineTotal);
                totalItems += qty;
            }
        }

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cart.getId());
        cartDto.setTotalItems(totalItems);
        cartDto.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        cartDto.setItems(itemDtos);
        return cartDto;
    }

    private OrderDto toOrderDto(Order order, OrderAddressDto addressDto, List<OrderItemDto> itemDtos) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setTax(order.getTax());
        dto.setShippingFee(order.getShippingFee());
        dto.setDiscount(order.getDiscount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(addressDto);
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setEstimatedDelivery(order.getEstimatedDelivery());
        dto.setOrderDate(order.getOrderDate());
        dto.setCancelEligibleUntil(order.getCancelEligibleUntil());
        dto.setItems(itemDtos);
        return dto;
    }
}
