package ru.home.user.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.home.user.security.jwt.JwtToken;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenValidationFilter extends OncePerRequestFilter {

    private final JwtToken jwtToken;

    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractToken(request);

        if (jwt == null) {
            log.trace("No Bearer token in request, skipping authentication");
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
            if (isTokenBlacklisted(jti)) {
                log.info("Rejected blacklisted token: jti={}", jti);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session is expired");
                return;
            }

            UUID userId = jwtToken.extractUserId(jwt);
            List<GrantedAuthority> authorities = jwtToken.extractAuthorities(jwt);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: userId={}, jti={}", userId, jti);

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            SecurityContextHolder.clearContext();

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            SecurityContextHolder.clearContext();
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
