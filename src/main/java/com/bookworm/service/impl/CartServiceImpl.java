package com.bookworm.service.impl;

import com.bookworm.dto.request.AddToCartRequest;
import com.bookworm.dto.request.UpdateCartItemRequest;
import com.bookworm.dto.response.CartDto;
import com.bookworm.dto.response.CartItemDto;
import com.bookworm.entity.Book;
import com.bookworm.entity.Cart;
import com.bookworm.entity.CartItem;
import com.bookworm.entity.User;
import com.bookworm.entity.enums.BookFormat;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.BookRepository;
import com.bookworm.repository.CartItemRepository;
import com.bookworm.repository.CartRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        BookRepository bookRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(UUID currentUserId) {
        Cart cart = getOrCreateCartEntity(currentUserId);
        return toCartDto(cart);
    }

    @Override
    public CartDto addItemToCart(UUID currentUserId, AddToCartRequest request) {
        Cart cart = getOrCreateCartEntity(currentUserId);
        UUID bookId = request.getBookId();

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        int addQty = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId);

        if (existingItemOpt.isPresent()) {
            // Requirement 4: If item already exists in cart, increase quantity
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + addQty);
            cartItemRepository.save(existingItem);
        } else {
            // New item in cart
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(addQty);
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        // Re-fetch updated cart
        return toCartDto(getOrCreateCartEntity(currentUserId));
    }

    @Override
    public CartDto updateCartItemQuantity(UUID currentUserId, UUID cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartEntity(currentUserId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item does not belong to user cart");
        }

        int targetQuantity = request.getQuantity() != null ? request.getQuantity() : 0;

        if (targetQuantity <= 0) {
            // Requirement 5: If quantity is set to 0, remove item
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(targetQuantity);
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return toCartDto(getOrCreateCartEntity(currentUserId));
    }

    private Cart getOrCreateCartEntity(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setUpdatedAt(Instant.now());
            return cartRepository.save(newCart);
        });
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
}
