package com.bookworm.dto.request;

import com.bookworm.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotNull(message = "Shipping address is required")
    @Valid
    private OrderAddressDto shippingAddress;

    @Size(max = 50)
    private String couponCode;

    @Min(value = 0)
    @Builder.Default
    private Integer redeemGiftPoints = 0;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Valid
    private CardPaymentDetailsDto paymentDetails;

    public OrderAddressDto getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(OrderAddressDto shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public Integer getRedeemGiftPoints() { return redeemGiftPoints; }
    public void setRedeemGiftPoints(Integer redeemGiftPoints) { this.redeemGiftPoints = redeemGiftPoints; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public CardPaymentDetailsDto getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(CardPaymentDetailsDto paymentDetails) { this.paymentDetails = paymentDetails; }
}
