package com.bookworm.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutQuoteResponse {
    private Integer itemCount;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal deliveryCharges;
    private BigDecimal couponDiscount;
    private BigDecimal giftPointsDiscount;
    private BigDecimal totalAmount;

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getDeliveryCharges() { return deliveryCharges; }
    public void setDeliveryCharges(BigDecimal deliveryCharges) { this.deliveryCharges = deliveryCharges; }
    public BigDecimal getCouponDiscount() { return couponDiscount; }
    public void setCouponDiscount(BigDecimal couponDiscount) { this.couponDiscount = couponDiscount; }
    public BigDecimal getGiftPointsDiscount() { return giftPointsDiscount; }
    public void setGiftPointsDiscount(BigDecimal giftPointsDiscount) { this.giftPointsDiscount = giftPointsDiscount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
