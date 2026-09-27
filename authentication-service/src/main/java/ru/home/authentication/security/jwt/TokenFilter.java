package ru.home.authentication.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.lettuce.core.RedisConnectionException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.home.authentication.security.CustomUserDetailsService;

import java.io.IOException;
import java.net.ConnectException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtToken jwtToken;

    private final StringRedisTemplate redisTemplate;

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractToken(request);

        if (jwt == null) {
            log.trace("No JWT in request, skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.trace("Authentication already set, skipping filter");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jti = jwtToken.getJti(jwt);
            log.debug("Processing token jti={}", jti);

            if (isTokenBlacklisted(jti)) {
                log.info("Rejected blacklisted token: jti={}", jti);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session is expired");
                return;
            }

            UUID userId = jwtToken.getUserId(jwt);
            UserDetails userDetails = customUserDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Authenticated user: userId={}, jti={}", userId, jti);

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());

        } catch (UsernameNotFoundException e) {
            log.warn("User not found for valid token: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isTokenBlacklisted(String jti) {
        if (jti == null) return false;
        try {
            return redisTemplate.hasKey("blacklist:" + jti);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis unavailable, blacklist check skipped (fail-open): jti={}", jti, e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error checking blacklist: jti={}", jti, e);
            return false;
        }
    }
}
