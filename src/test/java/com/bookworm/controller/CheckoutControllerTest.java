package com.bookworm.controller;

import com.bookworm.controller.impl.CheckoutControllerImpl;
import com.bookworm.dto.request.CheckoutQuoteRequest;
import com.bookworm.dto.response.CheckoutQuoteResponse;
import com.bookworm.service.CheckoutService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutControllerImpl checkoutController;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userId.toString(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/v1/checkout/quote returns 200 OK and quote response")
    void testCalculateQuote_Success() {
        CheckoutQuoteRequest request = new CheckoutQuoteRequest();
        request.setCouponCode("SAVE100");

        CheckoutQuoteResponse mockQuote = new CheckoutQuoteResponse();
        mockQuote.setTotalAmount(new BigDecimal("470.00"));

        when(checkoutService.calculateQuote(userId, request)).thenReturn(mockQuote);

        ResponseEntity<CheckoutQuoteResponse> response = checkoutController.calculateCheckoutQuote(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalAmount()).isEqualByComparingTo(new BigDecimal("470.00"));
        verify(checkoutService, times(1)).calculateQuote(userId, request);
    }
}
