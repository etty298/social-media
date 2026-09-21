package ru.home.authentication.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.home.authentication.entities.Role;
import ru.home.authentication.entities.UserEntity;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtToken {

    @Value("${token.access.secret}")
    private String secret;

    @Value("${token.access.lifetime}")
    private String lifetime;

    private SecretKey getSecret() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserEntity user) {
        Instant expireInstant = Instant.now().plusMillis(Long.parseLong(lifetime));
        Date expireDate = Date.from(expireInstant);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claims(Map.of("role", user.getRole(), "jti", jti))
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(getSecret(), Jwts.SIG.HS256)
                .compact();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(getAllClaims(token).getSubject());
    }

    public Instant getExpiration(String token) {
        return getAllClaims(token).getExpiration().toInstant();
    }

    public Role getRole(String token) {
        Object role = getAllClaims(token).get("role");
        return role == null ? null : Role.valueOf(role.toString());
    }

    public String getJti(String token) {
        return getAllClaims(token).get("jti").toString();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser().verifyWith(getSecret()).build().parseSignedClaims(token).getPayload();
    }
}
