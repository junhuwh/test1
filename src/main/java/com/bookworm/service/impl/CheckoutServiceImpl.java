package com.bookworm.service.impl;

import com.bookworm.dto.request.CheckoutQuoteRequest;
import com.bookworm.dto.response.CheckoutQuoteResponse;
import com.bookworm.entity.Cart;
import com.bookworm.entity.CartItem;
import com.bookworm.repository.CartRepository;
import com.bookworm.service.CheckoutService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;

    public CheckoutServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public CheckoutQuoteResponse calculateQuote(UUID currentUserId, CheckoutQuoteRequest request) {
        Cart cart = cartRepository.findByUserId(currentUserId).orElse(null);

        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        if (cart != null && cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                BigDecimal price = item.getBook().getPrice() != null ? item.getBook().getPrice() : BigDecimal.ZERO;
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(qty)));
                itemCount += qty;
            }
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // Tax = subtotal * 0.12
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);

        // Shipping = 0 if subtotal >= 499 else 50
        BigDecimal deliveryCharges = BigDecimal.ZERO.setScale(2);
        if (subtotal.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(new BigDecimal("499.00")) < 0) {
            deliveryCharges = new BigDecimal("50.00").setScale(2);
        }

        // Coupon SAVE100 gives 100 discount
        BigDecimal couponDiscount = BigDecimal.ZERO.setScale(2);
        if (request != null && request.getCouponCode() != null && "SAVE100".equalsIgnoreCase(request.getCouponCode().trim())) {
            couponDiscount = new BigDecimal("100.00").min(subtotal).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal giftPointsDiscount = BigDecimal.ZERO.setScale(2);

        BigDecimal totalAmount = subtotal
            .add(tax)
            .add(deliveryCharges)
            .subtract(couponDiscount)
            .subtract(giftPointsDiscount)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

        CheckoutQuoteResponse response = new CheckoutQuoteResponse();
        response.setItemCount(itemCount);
        response.setSubtotal(subtotal);
        response.setTax(tax);
        response.setDeliveryCharges(deliveryCharges);
        response.setCouponDiscount(couponDiscount);
        response.setGiftPointsDiscount(giftPointsDiscount);
        response.setTotalAmount(totalAmount);

        return response;
    }
}
