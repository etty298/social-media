package ru.home.user.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.home.user.api.dto.TokenValidationDto;
import ru.home.user.api.service.authentication.AuthenticationService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenValidationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Check if Authorization header is present and properly formatted
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authorization header required\",\"message\":\"Bearer token is required\"}");
            return;
        }
        
        String token = authHeader.substring(7);
        
        // Validate token with authentication service
        TokenValidationDto validationResult = authenticationService.validateToken(token);
        
        if (validationResult == null || !validationResult.isValid()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid token\",\"message\":\"Token is invalid or expired\"}");
            return;
        }
        
        // Token is valid - add user info to request attributes for controllers to use
        request.setAttribute("username", validationResult.getUsername());
        request.setAttribute("email", validationResult.getEmail());
        request.setAttribute("authenticated", true);
        
        filterChain.doFilter(request, response);
    }
}
