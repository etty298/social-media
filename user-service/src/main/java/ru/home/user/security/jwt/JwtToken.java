package ru.home.user.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

@Component
public class JwtToken {

    @Value("${token.access.secret}")
    private String secret;

    private SecretKey getSecret() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser().verifyWith(getSecret()).build().parseSignedClaims(token).getPayload();
    }

    public String getJti(String token) {
        Object jti = getAllClaims(token).get("jti");
        return jti != null ? jti.toString() : null;
    }

    public UUID extractUserId(String token) {
        String subject = getAllClaims(token).getSubject();
        if (subject == null) {
            throw new IllegalArgumentException("Token has no subject");
        }
        return UUID.fromString(subject);
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        Object role = getAllClaims(token).get("role");
        if (role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
