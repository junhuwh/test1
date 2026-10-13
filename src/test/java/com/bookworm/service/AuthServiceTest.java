package com.bookworm.service;

import com.bookworm.config.JwtConfig;
import com.bookworm.dto.request.LoginRequest;
import com.bookworm.dto.request.RegisterRequest;
import com.bookworm.dto.response.AuthResponse;
import com.bookworm.entity.Cart;
import com.bookworm.entity.User;
import com.bookworm.entity.enums.Role;
import com.bookworm.exception.DuplicateResourceException;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.repository.CartRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtConfig jwtConfig;
    private AuthServiceImpl authService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("alice@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setFullName("Alice Smith");
        testUser.setRole(Role.ROLE_CUSTOMER);
        testUser.setGiftPoints(100);

        jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtConfig, "expirationMs", 86400000L);

        authService = new AuthServiceImpl(userRepository, cartRepository, passwordEncoder, jwtConfig);
    }

    @Test
    @DisplayName("register should encode password, save user, create empty cart, and return AuthResponse")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");
        request.setFullName("Alice Smith");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(jwtConfig.validateToken(response.getToken())).isTrue();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.ROLE_CUSTOMER);
        assertThat(response.getUser().getGiftPoints()).isEqualTo(100);

        verify(userRepository, times(1)).existsByEmail("alice@example.com");
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(userRepository, times(1)).save(any(User.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("register should throw DuplicateResourceException when email already exists")
    void testRegister_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");
        request.setFullName("Alice Smith");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        assertThat(ex.getMessage()).contains("already exists");
        verify(userRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("login should return AuthResponse with JWT token when credentials match")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", testUser.getPasswordHash())).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(jwtConfig.validateToken(response.getToken())).isTrue();
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        verify(userRepository, times(1)).findByEmail("alice@example.com");
        verify(passwordEncoder, times(1)).matches("Password123!", testUser.getPasswordHash());
    }

    @Test
    @DisplayName("login should throw UnauthorizedException when password does not match")
    void testLogin_WrongPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("WrongPassword");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash())).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertThat(ex.getMessage()).contains("Invalid email or password");
    }

    @Test
    @DisplayName("login should throw UnauthorizedException when user does not exist")
    void testLogin_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertThat(ex.getMessage()).contains("Invalid email or password");
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
