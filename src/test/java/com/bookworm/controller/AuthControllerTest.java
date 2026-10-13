package com.bookworm.controller;

import com.bookworm.controller.impl.AuthControllerImpl;
import com.bookworm.dto.request.LoginRequest;
import com.bookworm.dto.request.RegisterRequest;
import com.bookworm.dto.response.AuthResponse;
import com.bookworm.dto.response.UserDto;
import com.bookworm.entity.enums.Role;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthControllerImpl authController;

    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        UserDto userDto = new UserDto();
        userDto.setId(UUID.randomUUID());
        userDto.setEmail("alice@example.com");
        userDto.setFullName("Alice Smith");
        userDto.setRole(Role.ROLE_CUSTOMER);
        userDto.setGiftPoints(100);

        testAuthResponse = new AuthResponse("mock-jwt-token", userDto);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register returns 201 Created and AuthResponse")
    void testRegister_ReturnsCreated() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");
        request.setFullName("Alice Smith");

        when(authService.register(request)).thenReturn(testAuthResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getBody().getUser().getEmail()).isEqualTo("alice@example.com");
        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 OK and AuthResponse")
    void testLogin_ReturnsOk() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(authService.login(request)).thenReturn(testAuthResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("mock-jwt-token");
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login propagates UnauthorizedException on failed login")
    void testLogin_Unauthorized_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("WrongPassword");

        when(authService.login(request)).thenThrow(new UnauthorizedException("Invalid email or password"));

        assertThrows(UnauthorizedException.class, () -> authController.login(request));
        verify(authService, times(1)).login(request);
    }
}
