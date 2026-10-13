package com.bookworm.service;

import com.bookworm.dto.request.CheckoutQuoteRequest;
import com.bookworm.dto.response.CheckoutQuoteResponse;
import com.bookworm.entity.Book;
import com.bookworm.entity.Cart;
import com.bookworm.entity.CartItem;
import com.bookworm.repository.CartRepository;
import com.bookworm.service.impl.CheckoutServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private UUID userId;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testCart = new Cart();
        testCart.setId(UUID.randomUUID());
        testCart.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("calculateQuote should apply free shipping when subtotal >= 499 and discount for SAVE100")
    void testCalculateQuote_FreeShippingAndCoupon() {
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("149.00"));

        Book book2 = new Book();
        book2.setPrice(new BigDecimal("359.00"));

        CartItem item1 = new CartItem();
        item1.setBook(book1);
        item1.setQuantity(1);

        CartItem item2 = new CartItem();
        item2.setBook(book2);
        item2.setQuantity(1);

        testCart.getItems().add(item1);
        testCart.getItems().add(item2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        CheckoutQuoteRequest request = new CheckoutQuoteRequest();
        request.setCouponCode("SAVE100");

        CheckoutQuoteResponse quote = checkoutService.calculateQuote(userId, request);

        // Subtotal = 149 + 359 = 508.00
        assertThat(quote.getSubtotal()).isEqualByComparingTo(new BigDecimal("508.00"));
        assertThat(quote.getItemCount()).isEqualTo(2);
        // Tax = 508 * 0.12 = 60.96
        assertThat(quote.getTax()).isEqualByComparingTo(new BigDecimal("60.96"));
        // Free shipping because subtotal >= 499
        assertThat(quote.getDeliveryCharges()).isEqualByComparingTo(BigDecimal.ZERO);
        // SAVE100 gives 100 discount
        assertThat(quote.getCouponDiscount()).isEqualByComparingTo(new BigDecimal("100.00"));
        // Total = 508 + 60.96 + 0 - 100 = 468.96
        assertThat(quote.getTotalAmount()).isEqualByComparingTo(new BigDecimal("468.96"));
    }

    @Test
    @DisplayName("calculateQuote should charge 50 shipping when subtotal < 499 and no coupon")
    void testCalculateQuote_StandardShipping() {
        Book book = new Book();
        book.setPrice(new BigDecimal("149.00"));

        CartItem item = new CartItem();
        item.setBook(book);
        item.setQuantity(1);

        testCart.getItems().add(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        CheckoutQuoteRequest request = new CheckoutQuoteRequest();

        CheckoutQuoteResponse quote = checkoutService.calculateQuote(userId, request);

        assertThat(quote.getSubtotal()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(quote.getDeliveryCharges()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(quote.getCouponDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
