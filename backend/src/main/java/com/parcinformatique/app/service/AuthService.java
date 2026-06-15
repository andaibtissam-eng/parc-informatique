package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.auth.AuthResponse;
import com.parcinformatique.app.dto.auth.ForgotPasswordRequest;
import com.parcinformatique.app.dto.auth.LoginRequest;
import com.parcinformatique.app.dto.auth.RegistrationResponse;
import com.parcinformatique.app.dto.auth.RefreshTokenRequest;
import com.parcinformatique.app.dto.auth.RegisterRequest;
import com.parcinformatique.app.dto.auth.ResetPasswordRequest;
import com.parcinformatique.app.dto.auth.VerifyEmailRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    RegistrationResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest);

    void logout(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(VerifyEmailRequest request);
}
