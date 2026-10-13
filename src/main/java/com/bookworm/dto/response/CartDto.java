package com.bookworm.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private UUID cartId;
    private Integer totalItems;
    private BigDecimal subtotal;
    @Builder.Default
    private List<CartItemDto> items = new ArrayList<>();

    public UUID getCartId() { return cartId; }
    public void setCartId(UUID cartId) { this.cartId = cartId; }
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public List<CartItemDto> getItems() { return items != null ? items : new ArrayList<>(); }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
