package ru.home.authentication.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.home.authentication.security.CustomUserDetailsService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtToken jwtToken;

    private final StringRedisTemplate redisTemplate;

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = null;
        UUID userId = null;
        UserDetails userDetails;
        UsernamePasswordAuthenticationToken authenticationToken;

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }

            if (redisTemplate.hasKey("blacklist:" + jwtToken.getJti(jwt))) {
                throw new AccessDeniedException("Your session is expired");
            }

            if (jwt != null) {
                try {
                    userId = jwtToken.getUserId(jwt);
                } catch (ExpiredJwtException e) {
                    //TODO
                }
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userDetails = customUserDetailsService.loadUserById(userId);
                    authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception e) {
            //TODO
        }
        filterChain.doFilter(request, response);
    }
}
