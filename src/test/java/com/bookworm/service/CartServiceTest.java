package com.bookworm.service;

import com.bookworm.dto.request.AddToCartRequest;
import com.bookworm.dto.request.UpdateCartItemRequest;
import com.bookworm.dto.response.CartDto;
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
import com.bookworm.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID userId;
    private UUID cartId;
    private UUID bookId;
    private Cart testCart;
    private Book testBook;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("charlie@example.com");

        testCart = new Cart();
        testCart.setId(cartId);
        testCart.setUser(testUser);
        testCart.setUpdatedAt(Instant.now());
        testCart.setItems(new ArrayList<>());

        testBook = new Book();
        testBook.setId(bookId);
        testBook.setTitle("Joy of Minimalism");
        testBook.setAuthorName("Daniel Reed");
        testBook.setPrice(new BigDecimal("149.00"));
        testBook.setFormat(BookFormat.Paperback);
    }

    @Test
    @DisplayName("getCart should return empty CartDto with subtotal 0.00 when cart is empty")
    void testGetCart_EmptyCart_Success() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        CartDto result = cartService.getCart(userId);

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(cartId);
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("addItemToCart should add a new item when item is not in cart")
    void testAddItemToCart_NewItem_Success() {
        AddToCartRequest request = new AddToCartRequest();
        request.setBookId(bookId);
        request.setQuantity(1);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByCartIdAndBookId(cartId, bookId)).thenReturn(Optional.empty());

        // When saved, add to testCart items list to reflect in-memory DB behavior
        doAnswer(invocation -> {
            CartItem savedItem = invocation.getArgument(0);
            savedItem.setId(UUID.randomUUID());
            testCart.getItems().add(savedItem);
            return savedItem;
        }).when(cartItemRepository).save(any(CartItem.class));

        CartDto result = cartService.addItemToCart(userId, request);

        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("149.00"));
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getTitle()).isEqualTo("Joy of Minimalism");
        assertThat(result.getItems().get(0).getTotalPrice()).isEqualByComparingTo(new BigDecimal("149.00"));

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addItemToCart should increase quantity when item already exists in cart")
    void testAddItemToCart_ExistingItem_IncreasesQuantity() {
        CartItem existingItem = new CartItem();
        existingItem.setId(UUID.randomUUID());
        existingItem.setCart(testCart);
        existingItem.setBook(testBook);
        existingItem.setQuantity(2);
        testCart.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest();
        request.setBookId(bookId);
        request.setQuantity(3);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByCartIdAndBookId(cartId, bookId)).thenReturn(Optional.of(existingItem));

        CartDto result = cartService.addItemToCart(userId, request);

        assertThat(result).isNotNull();
        assertThat(existingItem.getQuantity()).isEqualTo(5); // 2 + 3 = 5
        assertThat(result.getTotalItems()).isEqualTo(5);
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("745.00")); // 5 * 149 = 745

        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    @DisplayName("updateCartItemQuantity should update quantity and recalculate subtotal")
    void testUpdateCartItemQuantity_PositiveQuantity_Success() {
        UUID itemId = UUID.randomUUID();
        CartItem existingItem = new CartItem();
        existingItem.setId(itemId);
        existingItem.setCart(testCart);
        existingItem.setBook(testBook);
        existingItem.setQuantity(1);
        testCart.getItems().add(existingItem);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(4);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        CartDto result = cartService.updateCartItemQuantity(userId, itemId, request);

        assertThat(result).isNotNull();
        assertThat(existingItem.getQuantity()).isEqualTo(4);
        assertThat(result.getTotalItems()).isEqualTo(4);
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("596.00")); // 4 * 149 = 596

        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    @DisplayName("updateCartItemQuantity should delete item when quantity is set to 0")
    void testUpdateCartItemQuantity_ZeroQuantity_RemovesItem() {
        UUID itemId = UUID.randomUUID();
        CartItem existingItem = new CartItem();
        existingItem.setId(itemId);
        existingItem.setCart(testCart);
        existingItem.setBook(testBook);
        existingItem.setQuantity(2);
        testCart.getItems().add(existingItem);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        doAnswer(invocation -> {
            testCart.getItems().remove(existingItem);
            return null;
        }).when(cartItemRepository).delete(existingItem);

        CartDto result = cartService.updateCartItemQuantity(userId, itemId, request);

        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getItems()).isEmpty();

        verify(cartItemRepository, times(1)).delete(existingItem);
    }

    @Test
    @DisplayName("addItemToCart should throw ResourceNotFoundException when book does not exist")
    void testAddItemToCart_BookNotFound_ThrowsException() {
        UUID nonExistentBookId = UUID.randomUUID();
        AddToCartRequest request = new AddToCartRequest();
        request.setBookId(nonExistentBookId);
        request.setQuantity(1);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItemToCart(userId, request));
        verify(cartItemRepository, never()).save(any());
    }
}
