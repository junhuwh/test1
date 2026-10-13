package com.bookworm.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutQuoteRequest {
    @Size(max = 50)
    private String couponCode;

    @Min(value = 0, message = "Gift points cannot be negative")
    @Builder.Default
    private Integer redeemGiftPoints = 0;

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public Integer getRedeemGiftPoints() { return redeemGiftPoints; }
    public void setRedeemGiftPoints(Integer redeemGiftPoints) { this.redeemGiftPoints = redeemGiftPoints; }
}
