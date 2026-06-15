package com.parcinformatique.app.security;

import com.parcinformatique.app.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final Key key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
        List<String> roles = principal.getUser().getRoles().stream().map(role -> role.getName()).toList();

        return Jwts.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(principal.getUsername())
            .claim("uid", principal.getId().toString())
            .claim("roles", roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
        return Jwts.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        Claims claims = getClaims(token);
        String uid = claims.get("uid", String.class);
        if (uid == null && "refresh".equals(claims.get("type", String.class))) {
            uid = claims.getSubject();
        }
        return UUID.fromString(uid);
    }

    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public long getAccessTokenExpiresInSeconds() {
        return jwtProperties.getAccessTokenExpirationMinutes() * 60;
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload();
    }
}
