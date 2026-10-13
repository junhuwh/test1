package com.bookworm.service.impl;

import com.bookworm.config.JwtConfig;
import com.bookworm.dto.request.LoginRequest;
import com.bookworm.dto.request.RegisterRequest;
import com.bookworm.dto.response.AuthResponse;
import com.bookworm.dto.response.UserDto;
import com.bookworm.entity.Cart;
import com.bookworm.entity.User;
import com.bookworm.entity.enums.Role;
import com.bookworm.exception.DuplicateResourceException;
import com.bookworm.exception.UnauthorizedException;
import com.bookworm.repository.CartRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    public AuthServiceImpl(
        UserRepository userRepository,
        CartRepository cartRepository,
        PasswordEncoder passwordEncoder,
        JwtConfig jwtConfig
    ) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + normalizedEmail + " already exists");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setRole(Role.ROLE_CUSTOMER);
        user.setGiftPoints(100);

        User savedUser = userRepository.save(user);

        // Automatically create an empty shopping cart for the new user
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        String token = jwtConfig.generateToken(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole().name()
        );

        UserDto userDto = toUserDto(savedUser);
        return new AuthResponse(token, userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtConfig.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
        );

        UserDto userDto = toUserDto(user);
        return new AuthResponse(token, userDto);
    }

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setGiftPoints(user.getGiftPoints());
        return dto;
    }
}
