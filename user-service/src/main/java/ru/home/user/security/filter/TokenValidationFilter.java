package ru.home.user.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.home.user.security.jwt.JwtToken;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenValidationFilter extends OncePerRequestFilter {

    private final JwtToken jwtToken;

    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        
        // Check if Authorization header is present and properly formatted
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            Claims claims = jwtToken.getAllClaims(token);

            String jti = claims.get("jti").toString();

            if (redisTemplate.hasKey("blacklist:" + jti)) {
                throw new AccessDeniedException("Your session is expired");
            }

            UUID userId = UUID.fromString(claims.getSubject());

            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + claims.get("role"));


            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Token is valid - add user info to request attributes for controllers to use
            request.setAttribute("userId", userId);
        }
        
        filterChain.doFilter(request, response);
    }
}
