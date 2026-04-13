package com.fitdiary.controller;
import com.fitdiary.dto.AuthResponse;
import com.fitdiary.dto.LoginRequest;
import com.fitdiary.dto.RefreshTokenRequest;
import com.fitdiary.dto.RegisterRequest;
import com.fitdiary.dto.ApiResponse;
import com.fitdiary.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registrazione, login e refresh token")
class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registra un nuovo utente")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.register(req)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login con email e password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        System.out.println("Login attempt for email: " + req.getEmail());
        System.out.println("Password: " + req.getPassword());
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rinnova access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(req)));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout (invalida refresh token)")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.ok("Logout effettuato", null));
    }
}