package com.expense.service.impl;

import com.expense.dto.request.LoginRequest;
import com.expense.dto.request.RefreshTokenRequest;
import com.expense.dto.request.RegisterRequest;
import com.expense.dto.response.AuthResponse;
import com.expense.dto.response.RegisterResponse;
import com.expense.entity.RefreshToken;
import com.expense.entity.User;
import com.expense.enums.Role;
import com.expense.repository.RefreshTokenRepository;
import com.expense.repository.UserRepository;
import com.expense.security.CustomUserDetailsService;
import com.expense.security.JwtService;
import com.expense.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .message("User registered successfully")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return generateAuthResponse(user, userDetails);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        String rawToken = request.getRefreshToken();
        // Validate JWT signature and token type
        if (!"refresh".equals(jwtService.extractTokenType(rawToken))) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String tokenHash = hashToken(rawToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Refresh token not found or revoked"));

        if (storedToken.isRevoked()
                || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        User user = storedToken.getUser();

        if (!user.isActive()) {
            storedToken.setRevoked(true);
            throw new IllegalStateException("User account is inactive");
        }

        String tokenEmail = jwtService.extractUsername(rawToken);

        if (!user.getEmail().equals(tokenEmail)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Rotate the refresh token
        storedToken.setRevoked(true);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        return generateAuthResponse(user, userDetails);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {

        String tokenHash = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> token.setRevoked(true));
    }

    private AuthResponse generateAuthResponse(User user, UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(hashToken(refreshToken))
                .user(user)
                .expiresAt(
                        LocalDateTime.now()
                                .plusNanos(refreshTokenExpiration * 1_000_000)
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Unable to hash refresh token", ex
            );
        }
    }
}
