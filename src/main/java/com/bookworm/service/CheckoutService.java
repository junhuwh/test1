package com.bookworm.service;

import com.bookworm.dto.request.CheckoutQuoteRequest;
import com.bookworm.dto.response.CheckoutQuoteResponse;

import java.util.UUID;

public interface CheckoutService {
    CheckoutQuoteResponse calculateQuote(UUID currentUserId, CheckoutQuoteRequest request);
}
