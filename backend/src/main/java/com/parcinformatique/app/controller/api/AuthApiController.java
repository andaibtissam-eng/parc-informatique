package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.auth.AuthResponse;
import com.parcinformatique.app.dto.auth.ForgotPasswordRequest;
import com.parcinformatique.app.dto.auth.LoginRequest;
import com.parcinformatique.app.dto.auth.RegistrationResponse;
import com.parcinformatique.app.dto.auth.RefreshTokenRequest;
import com.parcinformatique.app.dto.auth.RegisterRequest;
import com.parcinformatique.app.dto.auth.ResetPasswordRequest;
import com.parcinformatique.app.dto.auth.VerifyEmailRequest;
import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, httpRequest);
        attachCookies(authResponse, response);
        return ResponseEntity.ok(ApiResponse.ok("Connexion reussie", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) {
        RegistrationResponse registrationResponse = authService.register(request, httpRequest);
        return ResponseEntity.status(201).body(ApiResponse.ok("Inscription enregistree. Validation administrateur requise.", registrationResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
        @Valid @RequestBody RefreshTokenRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.refresh(request, httpRequest);
        attachCookies(authResponse, response);
        return ResponseEntity.ok(ApiResponse.ok("Session renouvelee", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok(ApiResponse.ok("Deconnexion reussie", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Lien de reinitialisation genere", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Mot de passe mis a jour", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("Adresse email verifiee", null));
    }

    private void attachCookies(AuthResponse authResponse, HttpServletResponse response) {
        response.addHeader("Set-Cookie", ResponseCookie.from("ACCESS_TOKEN", authResponse.accessToken())
            .httpOnly(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(authResponse.expiresInSeconds())
            .build()
            .toString());
        response.addHeader("Set-Cookie", ResponseCookie.from("REFRESH_TOKEN", authResponse.refreshToken())
            .httpOnly(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(7 * 24 * 3600)
            .build()
            .toString());
    }
}
