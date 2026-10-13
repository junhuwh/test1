package com.bookworm.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyAgainResponse {
    private UUID orderId;
    private Integer itemsAddedCount;
    private CartDto cart;
    private String message;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public Integer getItemsAddedCount() { return itemsAddedCount; }
    public void setItemsAddedCount(Integer itemsAddedCount) { this.itemsAddedCount = itemsAddedCount; }
    public CartDto getCart() { return cart; }
    public void setCart(CartDto cart) { this.cart = cart; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
