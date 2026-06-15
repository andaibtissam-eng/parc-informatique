package com.parcinformatique.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String issuer;
    private Long accessTokenExpirationMinutes;
    private Long refreshTokenExpirationDays;
    private String secret;
}
