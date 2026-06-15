package com.parcinformatique.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseCompatibilityInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_status_check");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_language_check");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS system_account boolean DEFAULT false");
        jdbcTemplate.execute("UPDATE users SET system_account = false WHERE system_account IS NULL");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS email_verified boolean NOT NULL DEFAULT false");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS failed_login_attempts integer NOT NULL DEFAULT 0");
    }
}
