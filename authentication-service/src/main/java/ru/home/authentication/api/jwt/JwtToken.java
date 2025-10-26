package ru.home.authentication.api.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.home.authentication.api.dto.UserDto;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtToken {

    @Value("${token.secret}")
    private String secret;

    @Value("${token.lifetime}")
    private String lifetime;

    private SecretKey getSecret() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication auth) {
        UserDto user = (UserDto) auth.getPrincipal();
        Instant expireInstant = Instant.now().plusMillis(Long.parseLong(lifetime));
        Date expireDate = Date.from(expireInstant);
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(getSecret(), Jwts.SIG.HS256)
                .compact();
    }

    public String getNameFromJwt(String token) {
        return Jwts.parser().verifyWith(getSecret()).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
