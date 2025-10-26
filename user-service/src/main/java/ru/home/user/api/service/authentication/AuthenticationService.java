package ru.home.user.api.service.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.home.user.api.dto.TokenValidationDto;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public TokenValidationDto validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<TokenValidationDto> response = restTemplate.exchange(
                    authServiceUrl + "/auth/validate-token",
                    HttpMethod.POST,
                    entity,
                    TokenValidationDto.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            return TokenValidationDto.builder()
                    .valid(false)
                    .username(null)
                    .email(null)
                    .build();
        }
    }
}
