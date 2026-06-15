package com.parcinformatique.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_SECONDS = 60;
    private static final int MAX_REQUESTS = 20;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!request.getRequestURI().contains("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();
        AttemptWindow window = attempts.computeIfAbsent(key, ignored -> new AttemptWindow(Instant.now().getEpochSecond(), 0));
        long now = Instant.now().getEpochSecond();
        if (now - window.windowStart() >= WINDOW_SECONDS) {
            window = new AttemptWindow(now, 0);
            attempts.put(key, window);
        }
        if (window.count() >= MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"success\":false,\"message\":\"Trop de tentatives, réessayez plus tard.\"}");
            return;
        }
        attempts.put(key, new AttemptWindow(window.windowStart(), window.count() + 1));
        filterChain.doFilter(request, response);
    }

    private record AttemptWindow(long windowStart, int count) {
    }
}
