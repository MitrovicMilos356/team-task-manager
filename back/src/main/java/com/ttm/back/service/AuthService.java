package com.ttm.back.service;

import com.ttm.back.dto.AuthResponse;
import com.ttm.back.dto.LoginRequest;
import com.ttm.back.dto.RegisterRequest;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.exception.ApiException;
import com.ttm.back.model.RefreshToken;
import com.ttm.back.model.Role;
import com.ttm.back.model.User;
import com.ttm.back.repository.RefreshTokenRepository;
import com.ttm.back.repository.UserRepository;
import com.ttm.back.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationDays;

    public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        @Value("${app.refresh-token.expiration-days}") long refreshTokenExpirationDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.member);
        user.setActive(true);

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = issueRefreshToken(user.getId());
        return new AuthResponse(token, refreshToken, UserResponse.fromEntity(user));
    }

    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(jwtService.hashToken(refreshToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));

        if (!user.isActive()) {
            refreshTokenRepository.delete(stored);
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        refreshTokenRepository.delete(stored);

        String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = issueRefreshToken(user.getId());
        return new AuthResponse(newAccessToken, newRefreshToken, UserResponse.fromEntity(user));
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(jwtService.hashToken(refreshToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.fromEntity(user);
    }

    private String issueRefreshToken(Long userId) {
        String rawToken = jwtService.generateRefreshToken();
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(jwtService.hashToken(rawToken));
        entity.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        refreshTokenRepository.save(entity);
        return rawToken;
    }
}
